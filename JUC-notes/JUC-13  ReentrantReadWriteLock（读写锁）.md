# JUC-13  ReentrantReadWriteLock（读写锁）

# 1、ReentrantReadWriteLock

#### 1.1  基本概念

对 `ReentrantReadWriteLock`​ 来说，它的**读锁是共享锁，写锁是独占锁。当读操作远远高于写操作时，这时候使用**  `ReentrantReadWriteLock`​  **让 读-读 可以并发，提高性能。**

- 独占锁：指该锁一次只能被一个线程所持有，对 `ReentrantLock`​ 和 `Synchronized`​ 而言都是独占锁。
- 共享锁：指该锁可以被多个线程锁持有。

**注意事项：**

- 读-读能共存、读-写不能共存、写-写不能共存
- 读锁不支持条件变量
- ​**重入时升级不支持**​：持有读锁的情况下去获取写锁会导致获取写锁永久等待，需要先释放读，再去获得写
- ​**重入时降级支持**​：持有写锁的情况下去获取读锁，造成只有当前线程会持有读锁，因为写锁会互斥其他的锁

构造方法：

- ​`public ReentrantReadWriteLock()`​：默认构造方法，非公平锁
- ​`public ReentrantReadWriteLock(boolean fair)`​：true 为公平锁

常用API：

- ​`public ReentrantReadWriteLock.ReadLock readLock()`​：返回读锁
- ​`public ReentrantReadWriteLock.WriteLock writeLock()`​：返回写锁
- ​`public void lock()`​：加锁
- ​`public void unlock()`​：解锁
- ​`public boolean tryLock()`​：尝试获取锁

#### 1.2  应用 -- 缓存

更新时，是先清缓存还是先更新数据库？

- 先清缓存

  ![3b342713ecb9ce75542c0784a6a79937](assets/3b342713ecb9ce75542c0784a6a79937-20250722190651-ohkrkoa.png)
- 先更新数据库

  ![d1a3fceaeb5f0eef4a6401e08a0253af](assets/d1a3fceaeb5f0eef4a6401e08a0253af-20250722190702-wbtdtss.png)

使用读写锁实现一个简单的按需加载缓存（核心：写操作 加 写锁；读操作 加 读锁）：

- 代码实现

  ```java
  class GenericCachedDao<T> {
      // HashMap 作为缓存非线程安全, 需要保护
      HashMap<SqlPair, T> map = new HashMap<>();

      ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
      GenericDao genericDao = new GenericDao();
      public int update(String sql, Object... params) {
          SqlPair key = new SqlPair(sql, params);
          // 加写锁, 防止其它线程对缓存读取和更改
          lock.writeLock().lock();
          try {
              int rows = genericDao.update(sql, params);
              map.clear();
              return rows;
          } finally {
              lock.writeLock().unlock();
          }
      }
      public T queryOne(Class<T> beanClass, String sql, Object... params) {
          SqlPair key = new SqlPair(sql, params);
          // 加读锁, 防止其它线程对缓存更改
          lock.readLock().lock();
          try {
              T value = map.get(key);
              if (value != null) {
                  return value;
              }
          } finally {
              lock.readLock().unlock();
          }
          // 加写锁, 防止其它线程对缓存读取和更改
          lock.writeLock().lock();
          try {
              // get 方法上面部分是可能多个线程进来的, 可能已经向缓存填充了数据
              // 为防止重复查询数据库, 再次验证
              T value = map.get(key);
              if (value == null) {
                  // 如果没有, 查询数据库
                  value = genericDao.queryOne(beanClass, sql, params);
                  map.put(key, value);
              }
              return value;
          } finally {
              lock.writeLock().unlock();
          }
      }
      // 作为 key 保证其是不可变的
      class SqlPair {
          private String sql;
          private Object[] params;
          public SqlPair(String sql, Object[] params) {
              this.sql = sql;
              this.params = params;
          }
          @Override
          public boolean equals(Object o) {
              if (this == o) {
                  return true;
              }
              if (o == null || getClass() != o.getClass()) {
                  return false;
              }
              SqlPair sqlPair = (SqlPair) o;
              return sql.equals(sqlPair.sql) &&
                      Arrays.equals(params, sqlPair.params);
          }
          @Override
          public int hashCode() {
              int result = Objects.hash(sql);
              result = 31 * result + Arrays.hashCode(params);
              return result;
          }
      }
  }
  ```
- 注意：

  以上实现体现的是读写锁的应用，保证缓存和数据库的一致性，但有下面的问题没有考虑：

  - 适合读多写少，如果写操作比较频繁，以上实现性能低
  - 没有考虑缓存容量
  - 没有考虑缓存过期
  - 只适合单机
  - 并发性还是低，目前只会用一把锁
  - 更新方法太过简单粗暴，清空了所有 key（考虑按类型分区或重新设计 key）

#### 1.3  读写锁加锁原理

- t1 线程：w.lock（**写锁**），成功上锁 state = 0_1

  其实该流程和 ReentrantLock 几乎是一样的。

  但是还是有一些区别的，比如state不太一样，因为state既要给读锁用，也要给写锁用，所以要将state分成两部分。

  写锁状态占了 state 的低 16 位，而读锁使用的是 state 的高 16 位。

  ![image](assets/image-20250722204550-ytye82p.png)

  ```java
  // lock()  -> sync.acquire(1);
  public void lock() {
      sync.acquire(1);
  }

  public final void acquire(int arg) {
      // 尝试获得写锁，获得写锁失败，将当前线程关联到一个 Node 对象上, 模式为独占模式 
      if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
          selfInterrupt();
  }
  ```

  ```java
  protected final boolean tryAcquire(int acquires) {
      Thread current = Thread.currentThread();
      int c = getState();
      // 获得低 16 位, 代表写锁的 state 计数
      int w = exclusiveCount(c);
      // 说明有读锁或者写锁
      if (c != 0) {
          // c != 0 and w == 0 表示有读锁，【读锁不能升级】，直接返回 false
          // w != 0 说明有写锁，写锁的拥有者不是自己，获取失败
          if (w == 0 || current != getExclusiveOwnerThread())
              return false;
          
          // 执行到这里只有一种情况：【写锁重入】，所以下面几行代码不存在并发
          if (w + exclusiveCount(acquires) > MAX_COUNT)
              throw new Error("Maximum lock count exceeded");
          // 写锁重入, 获得锁成功，没有并发，所以不使用 CAS
          setState(c + acquires);
          return true;
      }
      
      // c == 0，说明没有任何锁，判断写锁是否该阻塞，是 false 就尝试获取锁，失败返回 false
      if (writerShouldBlock() || !compareAndSetState(c, c + acquires))
          return false;
      // 获得锁成功，设置锁的持有线程为当前线程
      setExclusiveOwnerThread(current);
      return true;
  }
  // 非公平锁 writerShouldBlock 总是返回 false, 无需阻塞
  final boolean writerShouldBlock() {
      return false; 
  }
  // 公平锁会检查 AQS 队列中是否有前驱节点, 没有(false)才去竞争
  final boolean writerShouldBlock() {
      return hasQueuedPredecessors();
  }
  ```

  ‍
- t2线程 r.lock（**读锁**），进入读锁的 sync.acquireShared(1) 流程，首先会进入 tryAcquireShared 流程

  如果有写锁占据，那么 tryAcquireShared 返回 -1 表示失败

  tryAcquireShared 返回值表示：

  - -1 表示失败；
  - 0 表示成功，但后继节点不会继续唤醒（**在后面的信号量章节介绍，读写锁只会返回-1或者1**）；
  - 正数表示成功，而且数值是还有几个后继节点需要唤醒，读写锁返回 1。

  ![image](assets/image-20250722215251-m4tuid3.png)

  ‍

  ```java
  public void lock() {
      sync.acquireShared(1);
  }
  public final void acquireShared(int arg) {
      // tryAcquireShared 返回负数, 表示获取读锁失败
      if (tryAcquireShared(arg) < 0)
          doAcquireShared(arg);
  }
  ```

  ```java
  // 尝试以共享模式获取
  protected final int tryAcquireShared(int unused) {
      Thread current = Thread.currentThread();
      int c = getState();
      // exclusiveCount(c) 代表低 16 位, 写锁的 state，成立说明有线程持有写锁
      // 写锁的持有者不是当前线程，则获取读锁失败，【写锁允许降级】
      if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current)
          return -1;
      
      // 高 16 位，代表读锁的 state，共享锁分配出去的总次数
      int r = sharedCount(c);
      // 读锁是否应该阻塞
      if (!readerShouldBlock() &&	r < MAX_COUNT &&
          compareAndSetState(c, c + SHARED_UNIT)) {	// 尝试增加读锁计数
          // 加锁成功
          // 加锁之前读锁为 0，说明当前线程是第一个读锁线程
          if (r == 0) {
              firstReader = current;
              firstReaderHoldCount = 1;
          // 第一个读锁线程是自己就发生了读锁重入
          } else if (firstReader == current) {
              firstReaderHoldCount++;
          } else {
              // cachedHoldCounter 设置为当前线程的 holdCounter 对象，即最后一个获取读锁的线程
              HoldCounter rh = cachedHoldCounter;
              // 说明还没设置 rh
              if (rh == null || rh.tid != getThreadId(current))
                  // 获取当前线程的锁重入的对象，赋值给 cachedHoldCounter
                  cachedHoldCounter = rh = readHolds.get();
              // 还没重入
              else if (rh.count == 0)
                  readHolds.set(rh);
              // 重入 + 1
              rh.count++;
          }
          // 读锁加锁成功
          return 1;
      }
      // 逻辑到这 应该阻塞，或者 cas 加锁失败
      // 会不断尝试 for (;;) 获取读锁, 执行过程中无阻塞
      return fullTryAcquireShared(current);
  }
  // 非公平锁 readerShouldBlock 偏向写锁一些，看 AQS 阻塞队列中第一个节点是否是写锁，是则阻塞，反之不阻塞
  // 防止一直有读锁线程，导致写锁线程饥饿
  // true 则该阻塞, false 则不阻塞
  final boolean readerShouldBlock() {
      return apparentlyFirstQueuedIsExclusive();
  }
  final boolean readerShouldBlock() {
      return hasQueuedPredecessors();
  }
  ```

  ```java
  final int fullTryAcquireShared(Thread current) {
      // 当前读锁线程持有的读锁次数对象
      HoldCounter rh = null;
      for (;;) {
          int c = getState();
          // 说明有线程持有写锁
          if (exclusiveCount(c) != 0) {
              // 写锁不是自己则获取锁失败
              if (getExclusiveOwnerThread() != current)
                  return -1;
          } else if (readerShouldBlock()) {
              // 条件成立说明当前线程是 firstReader，当前锁是读忙碌状态，而且当前线程也是读锁重入
              if (firstReader == current) {
                  // assert firstReaderHoldCount > 0;
              } else {
                  if (rh == null) {
                      // 最后一个读锁的 HoldCounter
                      rh = cachedHoldCounter;
                      // 说明当前线程也不是最后一个读锁
                      if (rh == null || rh.tid != getThreadId(current)) {
                          // 获取当前线程的 HoldCounter
                          rh = readHolds.get();
                          // 条件成立说明 HoldCounter 对象是上一步代码新建的
                          // 当前线程不是锁重入，在 readerShouldBlock() 返回 true 时需要去排队
                          if (rh.count == 0)
                              // 防止内存泄漏
                              readHolds.remove();
                      }
                  }
                  if (rh.count == 0)
                      return -1;
              }
          }
          // 越界判断
          if (sharedCount(c) == MAX_COUNT)
              throw new Error("Maximum lock count exceeded");
          // 读锁加锁，条件内的逻辑与 tryAcquireShared 相同
          if (compareAndSetState(c, c + SHARED_UNIT)) {
              if (sharedCount(c) == 0) {
                  firstReader = current;
                  firstReaderHoldCount = 1;
              } else if (firstReader == current) {
                  firstReaderHoldCount++;
              } else {
                  if (rh == null)
                      rh = cachedHoldCounter;
                  if (rh == null || rh.tid != getThreadId(current))
                      rh = readHolds.get();
                  else if (rh.count == 0)
                      readHolds.set(rh);
                  rh.count++;
                  cachedHoldCounter = rh; // cache for release
              }
              return 1;
          }
      }
  }
  ```

  ‍
- 获取读锁失败，进入 sync.doAcquireShared(1) 流程开始阻塞，首先也是调用 addWaiter 添加节点，不同之处在于节点被设置为 Node.SHARED 模式而非Node.EXCLUSIVE 模式，注意此时 t2 仍处于活跃状态

  ‍

  ![image](assets/image-20250722215420-xeifona.png)

  ```java
  private void doAcquireShared(int arg) {
      // 将当前线程关联到一个 Node 对象上, 模式为共享模式
      final Node node = addWaiter(Node.SHARED);
      boolean failed = true;
      try {
          boolean interrupted = false;
          for (;;) {
              // 获取前驱节点
              final Node p = node.predecessor();
              // 如果前驱节点就头节点就去尝试获取锁
              if (p == head) {
                  // 再一次尝试获取读锁
                  int r = tryAcquireShared(arg);
                  // r >= 0 表示获取成功
                  if (r >= 0) {
                      //【这里会设置自己为头节点，唤醒相连的后序的共享节点】
                      setHeadAndPropagate(node, r);
                      p.next = null; // help GC
                      if (interrupted)
                          selfInterrupt();
                      failed = false;
                      return;
                  }
              }
              // 是否在获取读锁失败时阻塞      					 park 当前线程
              if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
                  interrupted = true;
          }
      } finally {
          if (failed)
              cancelAcquire(node);
      }
  }
  ```

  ‍
- 如果没有成功，在 doAcquireShared 内 for (;;) 循环一次，shouldParkAfterFailedAcquire 内把前驱节点的 waitStatus 改为 -1，再 for (;;) 循环一次尝试 tryAcquireShared，不成功在 parkAndCheckInterrupt() 处 park

  ![image](assets/image-20250722215734-z5mtg7q.png)

  ‍
- 这种状态下，假设又有 t3 r.lock，t4 w.lock，这期间 t1 仍然持有锁，就变成了下面的样子

  ‍

  ![image](assets/image-20250722220116-qj3npuk.png)

  ‍

#### 1.4  读写锁解锁原理

- **t1 w.unlock**， 写锁解锁，这时会走到写锁的 sync.release(1)流程，调用 sync.tryRelease(1)成功，变成下面的样子

  ‍

  ![178aace4a6e47cc4e7babd6c311e31ef](assets/178aace4a6e47cc4e7babd6c311e31ef-20250723185621-jbfaek5.png)

  ```java
  public void unlock() {
      // 释放锁
      sync.release(1);
  }
  public final boolean release(int arg) {
      // 尝试释放锁
      if (tryRelease(arg)) {
          Node h = head;
          // 头节点不为空并且不是等待状态不是 0，唤醒后继的非取消节点
          if (h != null && h.waitStatus != 0)
              unparkSuccessor(h);
          return true;
      }
      return false;
  }
  protected final boolean tryRelease(int releases) {
      if (!isHeldExclusively())
          throw new IllegalMonitorStateException();
      int nextc = getState() - releases;
      // 因为可重入的原因, 写锁计数为 0, 才算释放成功
      boolean free = exclusiveCount(nextc) == 0;
      if (free)
          setExclusiveOwnerThread(null);
      setState(nextc);
      return free;
  }
  ```

  ‍
- 唤醒流程 sync.unparkSuccessor，这时 t2 在 doAcquireShared 的 parkAndCheckInterrupt() 处恢复运行，继续循环，执行 tryAcquireShared 成功则让读锁计数+1

  ‍

  ![image](assets/image-20250723190607-gtggils.png)

  ‍
- 接下来 t2 调用 setHeadAndPropagate(node, 1)，它原本所在节点被置为头节点；还会检查下一个节点是否是 shared，如果是则调用 doReleaseShared() 将 head 的状态从 -1 改为 0 并唤醒下一个节点，

  ‍

  ![image](assets/image-20250723190746-pjsd4bc.png)

  ![image](assets/image-20250723191010-k5g2mhs.png)

  ‍

  ```java
  private void setHeadAndPropagate(Node node, int propagate) {
      Node h = head; 
      // 设置自己为 head 节点
      setHead(node);
      // propagate 表示有共享资源（例如共享读锁或信号量），为 0 就没有资源
      if (propagate > 0 || h == null || h.waitStatus < 0 ||
          (h = head) == null || h.waitStatus < 0) {
          // 获取下一个节点
          Node s = node.next;
          // 如果当前是最后一个节点，或者下一个节点是【等待共享读锁的节点】
          if (s == null || s.isShared())
              // 唤醒后继节点
              doReleaseShared();
      }
  }
  ```

  ```java
  private void doReleaseShared() {
      // 如果 head.waitStatus == Node.SIGNAL ==> 0 成功, 下一个节点 unpark
  	// 如果 head.waitStatus == 0 ==> Node.PROPAGATE
      for (;;) {
          Node h = head;
          if (h != null && h != tail) {
              int ws = h.waitStatus;
              // SIGNAL 唤醒后继
              if (ws == Node.SIGNAL) {
                  // 因为读锁共享，如果其它线程也在释放读锁，那么需要将 waitStatus 先改为 0
              	// 防止 unparkSuccessor 被多次执行
                  if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                      continue;  
                  // 唤醒后继节点
                  unparkSuccessor(h);
              }
              // 如果已经是 0 了，改为 -3，用来解决传播性
              else if (ws == 0 && !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                  continue;                
          }
          // 条件不成立说明被唤醒的节点非常积极，直接将自己设置为了新的 head，
          // 此时唤醒它的节点（前驱）执行 h == head 不成立，所以不会跳出循环，会继续唤醒新的 head 节点的后继节点
          if (h == head)                   
              break;
      }
  }
  ```

  ‍

  ‍

  - ‍
- 这时 t3 在 doAcquireShared 内 parkAndCheckInterrupt() 处恢复运行，**唤醒连续的所有的共享节点**

  ‍

  ![image](assets/image-20250723191332-na9suox.png)

  ```java
  // 替换头结点
  private void setHeadAndPropagate(Node node, int propagate) {
      Node h = head; // Record old head for check below
      setHead(node);
          
         if (propagate > 0 || h == null || h.waitStatus < 0 ||
              (h = head) == null || h.waitStatus < 0) {
              // 拿到当前节点的下一个 
              Node s = node.next;
              // 如果节点的状态是 shared的话，
              if (s == null || s.isShared())
                  doReleaseShared();
          }
  }
  ```

  ‍
- 下一个节点不是 shared 了，因此不会继续唤醒 t4 所在节点
- **t2 r.unlock，t3 r.unlock**，t2 进入 sync.releaseShared(1) 中，调用 tryReleaseShared(1) 让计数减一，但计数还不为零，

  ‍

  ![image](assets/image-20250723193338-i74trnf.png)

  ‍

  ‍

  ‍

  - ‍
- t3  进入sync.releaseShared(1) 中，调用 tryReleaseShared(1) ，同样让计数减一，计数为零，进入doReleaseShared() 将头节点从 -1 改为 0 并唤醒下一个节点

  ‍

  ![image](assets/image-20250723194027-maoojzx.png)

  ```java
  public void unlock() {
      sync.releaseShared(1);
  }
  public final boolean releaseShared(int arg) {
      if (tryReleaseShared(arg)) {
          doReleaseShared();
          return true;
      }
      return false;
  }
  ```

  ```java
  protected final boolean tryReleaseShared(int unused) {

      for (;;) {
          int c = getState();
          int nextc = c - SHARED_UNIT;
          // 读锁的计数不会影响其它获取读锁线程, 但会影响其它获取写锁线程，计数为 0 才是真正释放
          if (compareAndSetState(c, nextc))
              // 返回是否已经完全释放了 
              return nextc == 0;
      }
  }
  ```

  ‍
- t4 在 acquireQueued 中 parkAndCheckInterrupt 处恢复运行，再次 for (;;) 这次自己是头节点的临节点，并且没有其他节点竞争，tryAcquire(1) 成功，修改头结点，流程结束

  ‍

  ![image](assets/image-20250723194936-ta7in8f.png)

  ‍

‍

# 2、StampedLock

#### 2.1  基本概念

StampedLock：读写锁，该类自 JDK 8 加入，是为了进一步优化读性能

特点：

- 在使用读锁、写锁时都必须配合戳使用
- StampedLock 不支持条件变量
- StampedLock **不支持重入**

基本用法

- 加解读锁：

  ```java
  long stamp = lock.readLock();
  lock.unlockRead(stamp);			// 类似于 unpark，解指定的锁
  ```
- 加解写锁：

  ```java
  long stamp = lock.writeLock();
  lock.unlockWrite(stamp);
  ```
- 乐观读，StampedLock 支持 `tryOptimisticRead()`​ 方法，读取完毕后做一次**戳校验**，如果校验通过，表示这期间没有其他线程的写操作，数据可以安全使用，如果校验没通过，需要重新获取读锁，保证数据一致性

  ```java
  long stamp = lock.tryOptimisticRead();
  // 验戳
  if(!lock.validate(stamp)){
  	// 锁升级
  }
  ```

#### 2.2  示例

提供一个数据容器类内部分别使用读锁保护数据的 read() 方法，写锁保护数据的 write() 方法：

- 读-读可以优化
- 读-写优化读，补加读锁

```java
public static void main(String[] args) throws InterruptedException {
    DataContainerStamped dataContainer = new DataContainerStamped(1);
    new Thread(() -> {
    	dataContainer.read(1000);
    },"t1").start();
    Thread.sleep(500);
    
    new Thread(() -> {
        dataContainer.write(1000);
    },"t2").start();
}

class DataContainerStamped {
    private int data;
    private final StampedLock lock = new StampedLock();

    public int read(int readTime) throws InterruptedException {
        long stamp = lock.tryOptimisticRead();
        System.out.println(new Date() + " optimistic read locking" + stamp);
        Thread.sleep(readTime);
        // 戳有效，直接返回数据
        if (lock.validate(stamp)) {
            Sout(new Date() + " optimistic read finish..." + stamp);
            return data;
        }

        // 说明其他线程更改了戳，需要锁升级了，从乐观读升级到读锁
        System.out.println(new Date() + " updating to read lock" + stamp);
        try {
            stamp = lock.readLock();
            System.out.println(new Date() + " read lock" + stamp);
            Thread.sleep(readTime);
            System.out.println(new Date() + " read finish..." + stamp);
            return data;
        } finally {
            System.out.println(new Date() + " read unlock " +  stamp);
            lock.unlockRead(stamp);
        }
    }

    public void write(int newData) {
        long stamp = lock.writeLock();
        System.out.println(new Date() + " write lock " + stamp);
        try {
            Thread.sleep(2000);
            this.data = newData;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println(new Date() + " write unlock " + stamp);
            lock.unlockWrite(stamp);
        }
    }
}
```

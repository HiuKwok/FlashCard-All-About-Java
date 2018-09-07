package RxJava.basic;

import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
/**
 * 
 * @author Hiu.Kwok
 * 
 * Ref: Reactive Programming with RxJava Ch1
 * 
 * Key take-away:
 *   - (onNext(), onCompleted(), onError() ) can never be emitted concurrently.
 *   - To put stuff in one thread can make use of Ram & CPU optimization for sequential computation.
 *   	 - Such optimization can't be perform on Stream as the total item is unknown. 
 *   - What RxJava.parallel( ) is simply spill workload into multiple obserable and merge when all done. (Similar to the last example)
 *   - Observable by itself is lazy which mean it do nothing until subsribler is on. It's fundamentally different from Future
 *   you may encounter on J2SE. 
 *   
 *   
 *
 */
public class BasicUsage {
	
	
	

	private static void laziness () {
//		More to instruction-ish thing to told how to work once got subsribled. 
		Observable<String> someData = Observable.create(s -> {
		    s.onNext("Hello");
		    s.onComplete();
		});
//		Above code wouldn't execute until it got subscribed, more to execution plan-ish.

//		Observable unlike Stream || Future, can be reuse.
		someData.subscribe(s -> System.out.println(s) );
		someData.subscribe(s -> System.out.println(s + " HK") );
		
		
	}
	
	private static void multiThreadObservable() {

		/**
		 * Following code would pass compiler but it's error prone and never ever to do this.
		 * As Observable is design to use in sequential manner. (Concurrent is Ok but not Parallelism)
		 */
		Observable.create(s -> {
//		  // Thread A
//		  new Thread(() -> {
//		    s.onNext("one");
//		    s.onNext("two");
//		  }).start();
//
//		  // Thread B
//		  new Thread(() -> {
//		    s.onNext("three");
//		    s.onNext("four");
//		  }).start();

		});
		
		/**
		 * If two threads are insisted then spread into two Observable and merge.
		 * 
		 */
		Observable<String> a = Observable.create(s -> {
			new Thread( () -> {
				s.onNext("one");
				s.onNext("two");
				s.onComplete();
			}).start();
		});
		
		Observable<String> b = Observable.create(s -> {
			new Thread( () -> {
				s.onNext("three");
				s.onNext("four");
				s.onComplete();
			}).start();
		});
		
		Observable<String> c = Observable.merge(a, b);
		c.subscribe(s -> System.out.println(s));
		
	}
	
	private static void syncUsecase() {
		/**
		 * By following the logic above, it's no point to schedule a task which rely in-memory storage like collection and such.
		 * Cost of schedule task > short short waiting block time. 
		 */
		HashMap<String,String> cache = new HashMap<String, String> ();
		cache.put("HKSAR", "Hello HK");
		
		Observable.create(s -> {
//			Synchronously way
		    s.onNext(cache.get("HKSAR"));
		    s.onComplete();
		}).subscribe(value -> System.out.println(value));

		
		/**
		 * By the usual case on actual situation would be first lookup for in-mmeory and 
		 * fallback to fetch Db if not exist.
		 */
		Observable.create(s -> {
			
			//Look up cache.
			boolean inMemory = cache.containsKey("HK");
			
			if (inMemory) {
//				Sync way
			}else {
//				Async way
			}
			
		}).subscribe(s -> System.out.println(s));
		
		
		/**
		 * Stream is another common case which better to use Sync.
		 */
		Observable<Integer> o = Observable.create(s -> {
		    s.onNext(1);
		    s.onNext(2);
		    s.onNext(3);
		    s.onComplete();
		});
//		Most Observable function pipelines are sync
		o.map(i -> "Number " + i)
		 .subscribe(s -> System.out.println(s));	
		
	}

	private static void hello() {
//		Observable is suitable to use with non-blocking operation. 
		Observable.create(s -> {
//			Call subscribe method.
			s.onNext("Hello World!");
			s.onComplete();
//		And also in this case is not point of schedule the event, as the method it's is cheap. The effort of arrange to schedule 
//		is more costy compare with the method itself. 
		}).subscribe(hello -> System.out.println(hello));
		
	}
	
//	Future but reuseable and lazy invoke by nature. 
	/**
	 * Single would be preferable > Observable. As expected behavour 
	 * would be narrowed down to 3 types only.
	 *  - Respond with an error 
	 *  - Never respond
	 *  - REspond with a success
	 *  
	 *  Which compare to obserible the have more variety of return choice.
	 *  As result is returned as well on success case. (Additional states).
	 *  
	 *  - No data and term
	 *  - Single value and term
	 *  - Multiple value and term
	 *  - One or more values and never term (Wait for more data).
	 *  
	 */
	private static void lazyFuture () {
		
		Single<String> a = Single. <String>create( o-> {
			System.out.println("Single[A] triggered!");
			o.onSuccess("DataA");
		}).subscribeOn(Schedulers.io());
		
		
		Single<String> b = Single. <String>create( o-> {
			System.out.println("Single[B] triggered!");
			((Observable<String>) o).just("DataB");
		}).subscribeOn(Schedulers.io());
		

//		Merge both Single
		Flowable<String> c = Single.merge(a,b);
	}
	
	/**
	 * The whole purpose of Completable Obj is used to replace the usage of Observable<Void> && Single<void>.
	 */
	private static void CompletAble () {
		Completable log = Completable.create(s -> {
//			Stuff like logging and such.
			System.out.println("I am async write event");
			s.onComplete();
//			Or error with error object.
//			s.onError(new Throwable("Hello fail"));
		});
		
		log.subscribe();
	}
	
	
	public static void main(String[] args) {

//		hello();
//		laziness();
//		multiThreadObservable();
//		syncUsecase();
//		lazyFuture();
		
		CompletAble();

	
	}	
}

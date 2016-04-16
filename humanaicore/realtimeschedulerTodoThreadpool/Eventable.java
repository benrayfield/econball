/** Ben F Rayfield offers HumanAiCore opensource GNU LGPL */
package humanaicore.realtimeschedulerTodoThreadpool;

/** This is a more efficient and general replacement for Task.
(UPDATE: Task extends Eventable)
Instead of having to run on an interval andOr having a separate system for
separate keyboard/mouse/gameController events, all that will be done
in 1 func that takes 1 param. It can be called on a timer andOr
any kind of events. Its a simple expandable paradigm.
<br><br>
An object implementing Eventable should not just run 1 more cycle
of their calculation as a Task would do, because event(Object) may
be called many times in the same millisecond or less often
like would happen in a Task, and its probably not on event intervals,
so the object has to consider what time it is
<br><br>
If its a humanaicore.realtimeschedulerTodoThreadpool.TimedEvent,
use its time var which may slightly differ from Time.time()
because of running may times in a loop to simulate different parts
of a larger time cycle.
*/
public interface Eventable{

	public void event(Object context);

}
package space.jamestang.ktimer.transactions

import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import space.jamestang.ktimer.core.Constant
import java.util.concurrent.ConcurrentHashMap

object TaskPool {

    fun initialize(){
        Constant.logger.info("TaskPool initialized!")
    }

    private val timer = HashedWheelTimer()
    private val taskPool = ConcurrentHashMap<String, Timeout>()



    fun scheduleTask(taskId: String, delayMinute: Long, task: Runnable) {
        val timeout = timer.newTimeout({ timeout ->
            task.run()
            taskPool.remove(taskId)
        }, delayMinute, java.util.concurrent.TimeUnit.MINUTES)
        taskPool[taskId] = timeout
    }

    fun scheduleTask(taskId: String, delayMinute: Long, task: () -> Unit){
        val timeout = timer.newTimeout( {
            task()
            taskPool.remove(taskId)
        } , delayMinute, java.util.concurrent.TimeUnit.MINUTES)
        taskPool[taskId] = timeout
    }

    fun cancelTask(taskId: String) {
        val timeout = taskPool.remove(taskId)
        timeout?.cancel()
    }
}
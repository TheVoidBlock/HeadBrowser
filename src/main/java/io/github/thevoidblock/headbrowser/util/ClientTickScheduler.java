package io.github.thevoidblock.headbrowser.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.List;

public class ClientTickScheduler {
    private final static List<ScheduledTask> tasks = new ArrayList<>();

    public static void schedule(ClientTickEvents.EndTick task, int ticks) {
        tasks.add(new ScheduledTask(task, ticks));
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            List<ScheduledTask> completedTasks = new ArrayList<>();

            for(ScheduledTask scheduledTask : tasks) {
                if(scheduledTask.tick() == 0) {
                    scheduledTask.task.onEndTick(client);
                    completedTasks.add(scheduledTask);
                }
            }

            tasks.removeAll(completedTasks);
        });
    }

    private static class ScheduledTask {
        public final ClientTickEvents.EndTick task;
        private int ticks;

        public ScheduledTask(ClientTickEvents.EndTick task, int ticks) {
            this.task = task;
            this.ticks = ticks;
        }

        public int tick() {
            return ticks--;
        }
    }
}

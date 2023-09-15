package com.firesoftitan.play.titanbox.titanmachines.infos;

import com.firesoftitan.play.titanbox.titanmachines.runnables.PipeSubRunnable;

import java.util.UUID;

public class PipeRunnableInfo {
    private final UUID uuid;
    private long runtime = 0;

    public PipeRunnableInfo(UUID uuid) {
        this.uuid = uuid;
    }

    public void setRuntime(long runtime) {
        this.runtime = Math.max(runtime, this.runtime);
    }

    public UUID getUUID() {
        return uuid;
    }

    public long getRuntime() {
        return runtime;
    }
}

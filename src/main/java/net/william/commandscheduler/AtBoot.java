package net.william.commandscheduler;

public class AtBoot extends Scheduler {

    // IMPORTANT! Every subclass of BaseScheduledCommand needs a TYPENAME
    public static final String TYPENAME = "At-Boot";

    private transient boolean expired = false;

    public AtBoot(String ID, String command) {
        super(ID, true, command);
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired() {
        this.expired = true;
    }

    @Override
    public String toString() {
        return String.format("OnceAtBootCommand{id='%s', active=%s, command='%s', expired=%s}", getID(), isActive(),
                getCommand(), expired);

    }

}

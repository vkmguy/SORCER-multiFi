package computermaker.impl;

import computermaker.IComputer;
import sorcer.service.Context;

public class Computer implements IComputer {

    private String processor;
    private String memory;
    private String hardDrive;

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getHardDrive() {
        return hardDrive;
    }

    public void setHardDrive(String hardDrive) {
        this.hardDrive = hardDrive;
    }

    // implementaton to provide computer parts to our service
    ComputerParts computerParts = new ComputerParts();

    public Computer(String processor, String memory, String hardDrive) {
        this.processor = processor;
        this.memory = memory;
        this.hardDrive = hardDrive;
    }

    @Override
    public Context getMemory(Context context) {
        return computerParts.getMemory(context);
    }

    @Override
    public Context getProcessor(Context context) {

        return computerParts.getProcessor(context);
    }

    @Override
    public Context getHardDrive(Context context) {

        return computerParts.getHardDrive(context);
    }
}

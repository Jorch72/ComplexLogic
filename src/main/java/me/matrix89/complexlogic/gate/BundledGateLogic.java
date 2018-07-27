package me.matrix89.complexlogic.gate;

import net.minecraft.util.EnumFacing;
import pl.asie.simplelogic.gates.PartGate;
import pl.asie.simplelogic.gates.logic.GateLogic;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public abstract class BundledGateLogic extends GateLogic {
    private final Map<EnumFacing, byte[]> bundledValues = new EnumMap<>(EnumFacing.class);
    private EnumFacing[] horizontals = EnumFacing.HORIZONTALS;
    private boolean shouldUpdate = false;

    public BundledGateLogic() {
        for (EnumFacing horizontal : horizontals) {
            if (getType(horizontal) == Connection.OUTPUT_BUNDLED || getType(horizontal) == Connection.INPUT_BUNDLED) {
                bundledValues.put(horizontal, new byte[16]);
            }
        }
    }

    @Override
    public boolean tick(PartGate parent) {
        boolean change = false;

        for (EnumFacing facing : horizontals) {
            if (getType(facing) != Connection.INPUT_BUNDLED) continue;
            bundledValues.replace(facing, parent.getBundledInput(facing));
        }

        for (EnumFacing facing : horizontals) {
            if (getType(facing) != Connection.OUTPUT_BUNDLED) continue;
            byte[] newValue = calculateBundledOutput(facing);
            change |= !Arrays.equals(bundledValues.get(facing), newValue);
            if (change) {
                bundledValues.replace(facing, newValue);
            }

        }

        if(shouldUpdate) {
            shouldUpdate = false;
            return true;
        }
        return change || super.tick(parent);
    }

    public int bundledRsToDigi(byte[] values) {
        int out = 0;
        for (int i = 0; i < 16; i++) {
            out |= ((values[i] != 0) ? 1 : 0) << i;
        }
        return out;
    }

    public byte[] bundledDigiToRs(int value) {
        byte[] out = new byte[16];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) ((value & (1 << i)) != 0 ? 15 : 0);
        }
        return out;
    }

    public void setBundledValue(EnumFacing side, byte[] value) {
        bundledValues.replace(side, value);
        shouldUpdate = true;
    }

    public byte[] getInputValueBundled(EnumFacing side) {
        return bundledValues.getOrDefault(side, new byte[16]);
    }

    @Override
    public byte[] getOutputValueBundled(EnumFacing side) {
        return bundledValues.getOrDefault(side, new byte[16]);
    }

    @Override
    protected byte calculateOutputInside(EnumFacing enumFacing) {
        return outputValues[enumFacing.getIndex() - 1];
    }

    public byte[] calculateBundledOutput(EnumFacing facing) {
        return bundledValues.getOrDefault(facing, new byte[16]);
    }
}
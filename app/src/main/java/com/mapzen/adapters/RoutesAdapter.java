package com.mapzen.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mapzen.fragment.InstructionFragment;
import com.mapzen.fragment.RouteFragment;
import com.mapzen.osrm.Instruction;

import org.oscim.map.Map;

import java.util.ArrayList;

public class RoutesAdapter extends FragmentStatePagerAdapter {
    private ArrayList<Instruction> instructions = new ArrayList<Instruction>();
    private Map map;
    private RouteFragment parent;

    @Override
    public int getCount() {
        return instructions.size();
    }

    public void setInstructions(ArrayList<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public void setParent(RouteFragment parent) {
        this.parent = parent;
    }

    public RoutesAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        InstructionFragment instructionFragment = new InstructionFragment();
        instructionFragment.setInstruction(instructions.get(i));
        instructionFragment.setMap(map);
        instructionFragment.setParent(parent);
        if (getCount() > i + 1) {
            instructionFragment.setHasNext();
        }
        if (i != 0) {
            instructionFragment.setHasPrev();
        }
        return instructionFragment;
    }
}

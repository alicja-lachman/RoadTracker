package com.polsl.roadtracker.model;

import com.github.pires.obd.commands.protocol.ObdProtocolCommand;

/**
 * Created by m_lig on 06.06.2017.
 */

public class ObdSetDefaultCommand extends ObdProtocolCommand {

    public ObdSetDefaultCommand() {
        super("ATD");
    }

    public ObdSetDefaultCommand(ObdSetDefaultCommand other) {
        super(other);
    }

    @Override
    public String getFormattedResult() {
        return getResult();
    }

    @Override
    public String getName() {
        return "OBD set defaults";
    }
}

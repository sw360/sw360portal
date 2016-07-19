package com.siemens.sw360.datahandler.licenseinfo;

import org.apache.thrift.TException;

/**
 * @author: alex.borodin@evosoft.com
 */
public class UncheckedTException extends RuntimeException {
    public UncheckedTException(TException te) {
        super(te);
    }

    TException getTExceptionCause() {
        return (TException) getCause();
    }
}

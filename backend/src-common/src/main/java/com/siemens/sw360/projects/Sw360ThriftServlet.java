package com.siemens.sw360.projects;

import org.apache.log4j.Logger;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.apache.log4j.Logger.getLogger;

/**
 * Created by andreas on 27.11.15.
 */
public class Sw360ThriftServlet extends TServlet {
    private static final Logger log = getLogger(Sw360ThriftServlet.class);

    public Sw360ThriftServlet(TProcessor processor, TProtocolFactory protocolFactory) {
        super(processor, protocolFactory);
    }

    public Sw360ThriftServlet(TProcessor processor, TProtocolFactory inProtocolFactory, TProtocolFactory outProtocolFactory) {
        super(processor, inProtocolFactory, outProtocolFactory);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            super.doPost(request, response);
        } catch (Exception e) {
            log.error("uncaught", e);
            throw e;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            super.doGet(request, response);
        } catch (Exception e) {
            log.error("uncaught", e);
            throw e;
        }
    }
}
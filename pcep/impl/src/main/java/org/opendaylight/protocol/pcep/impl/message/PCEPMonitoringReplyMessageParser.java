/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.protocol.pcep.impl.message;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.protocol.pcep.spi.AbstractMessageParser;
import org.opendaylight.protocol.pcep.spi.MessageUtil;
import org.opendaylight.protocol.pcep.spi.ObjectRegistry;
import org.opendaylight.protocol.pcep.spi.PCEPDeserializerException;
import org.opendaylight.protocol.pcep.spi.PCEPErrors;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.message.rev131007.Pcmonrep;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.message.rev131007.PcmonrepBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.Object;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.monitoring.metrics.MetricPce;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.monitoring.metrics.MetricPceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.monitoring.object.Monitoring;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.monitoring.response.monitoring.metrics.list.GeneralMetricsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.monitoring.response.monitoring.metrics.list.GeneralMetricsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.monitoring.response.monitoring.metrics.list.SpecificMetricsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.monitoring.response.monitoring.metrics.list.SpecificMetricsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.monitoring.response.monitoring.metrics.list.specific.metrics.list.SpecificMetrics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.monitoring.response.monitoring.metrics.list.specific.metrics.list.SpecificMetricsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.overload.object.Overload;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.pcc.id.req.object.PccIdReq;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.pce.id.object.PceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.pcmonrep.message.PcmonrepMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.pcmonrep.message.PcmonrepMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.proc.time.object.ProcTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.rp.object.Rp;

/**
 * Parser for {@link Pcmonrep}
 * @see https://tools.ietf.org/html/rfc5886#section-3.2
 */
public class PCEPMonitoringReplyMessageParser extends AbstractMessageParser {

    public static final int TYPE = 9;

    public PCEPMonitoringReplyMessageParser(final ObjectRegistry registry) {
        super(registry);
    }

    @Override
    public void serializeMessage(final Message message, final ByteBuf buffer) {
        Preconditions.checkArgument(message instanceof Pcmonrep, "Wrong instance of Message. Passed instance of %s. Need Pcmonrep.", message.getClass());
        final PcmonrepMessage monRepMsg = ((Pcmonrep) message).getPcmonrepMessage();
        Preconditions.checkArgument(monRepMsg.getMonitoring() != null, "MONITORING object is mandatory.");
        final ByteBuf body = Unpooled.buffer();
        serializeObject(monRepMsg.getMonitoring(), body);
        serializeObject(monRepMsg.getPccIdReq(), body);
        if (monRepMsg.getMonitoringMetricsList() instanceof GeneralMetricsList) {
            final GeneralMetricsList gml = (GeneralMetricsList) monRepMsg.getMonitoringMetricsList();
            if (gml.getMetricPce() != null) {
                for (final MetricPce metricPce : gml.getMetricPce()) {
                    serializeMetricPce(metricPce, body);
                }
            }
        } else if (monRepMsg.getMonitoringMetricsList() instanceof SpecificMetricsList) {
            final SpecificMetricsList sml = (SpecificMetricsList) monRepMsg.getMonitoringMetricsList();
            if (sml.getSpecificMetrics() != null) {
                for (final SpecificMetrics specificMetrics : sml.getSpecificMetrics()) {
                    serializeObject(specificMetrics.getRp(), body);
                    if (specificMetrics.getMetricPce() != null) {
                        for (final MetricPce metricPce : specificMetrics.getMetricPce()) {
                            serializeMetricPce(metricPce, body);
                        }
                    }
                }
            }
        }

        MessageUtil.formatMessage(TYPE, body, buffer);
    }

    private void serializeMetricPce(final MetricPce metricPce, final ByteBuf buffer) {
        Preconditions.checkArgument(metricPce.getPceId() != null, "PCE-ID must be present.");
        serializeObject(metricPce.getPceId(), buffer);
        serializeObject(metricPce.getProcTime(), buffer);
        serializeObject(metricPce.getOverload(), buffer);
    }

    @Override
    protected Message validate(final List<Object> objects, final List<Message> errors) throws PCEPDeserializerException {
        if (objects == null) {
            throw new IllegalArgumentException("Passed list can't be null.");
        }
        if (objects.isEmpty()) {
            throw new PCEPDeserializerException("Pcmonrep message cannot be empty.");
        }
        if (!(objects.get(0) instanceof Monitoring)) {
            errors.add(createErrorMsg(PCEPErrors.MONITORING_OBJECT_MISSING, Optional.<Rp>absent()));
            return null;
        }
        final PcmonrepMessageBuilder builder = new PcmonrepMessageBuilder();
        builder.setMonitoring((Monitoring) objects.get(0));
        objects.remove(0);
        if (!objects.isEmpty() && objects.get(0) instanceof PccIdReq) {
            builder.setPccIdReq((PccIdReq) objects.get(0));
            objects.remove(0);
        }
        final List<SpecificMetrics> specificMetrics = new ArrayList<>();
        while (!objects.isEmpty()) {
            final SpecificMetricsBuilder smb = new SpecificMetricsBuilder();
            final List<MetricPce> metricPceList = new ArrayList<>();
            if (objects.get(0) instanceof Rp) {
                smb.setRp((Rp) objects.get(0));
                objects.remove(0);
            }
            while (!objects.isEmpty() && !(objects.get(0) instanceof Rp)) {
                metricPceList.add(validateMonitoringMetrics(objects));
            }
            if (smb.getRp() != null) {
                smb.setMetricPce(metricPceList);
                specificMetrics.add(smb.build());
            } else if (!metricPceList.isEmpty()) {
                builder.setMonitoringMetricsList(new GeneralMetricsListBuilder().setMetricPce(metricPceList).build());
            }
        }
        if (!specificMetrics.isEmpty()) {
            builder.setMonitoringMetricsList(new SpecificMetricsListBuilder().setSpecificMetrics(specificMetrics).build());
        }
        if (!objects.isEmpty()) {
            throw new PCEPDeserializerException("Unprocessed Objects: " + objects);
        }
        return new PcmonrepBuilder().setPcmonrepMessage(builder.build()).build();
    }

    private MetricPce validateMonitoringMetrics(final List<Object> objects) throws PCEPDeserializerException {
        final MetricPceBuilder metricPceBuilder = new MetricPceBuilder();
        if (!(objects.get(0) instanceof PceId)) {
            throw new PCEPDeserializerException("metric-pce-list must start with PCE-ID object.");
        }
        metricPceBuilder.setPceId((PceId) (objects.get(0)));
        objects.remove(0);
        State state = State.START;
        while (!objects.isEmpty() && !state.equals(State.END)) {
            final Object obj = objects.get(0);
            switch(state) {
            case START :
                state = State.PROC_TIME;
                if (obj instanceof ProcTime) {
                    metricPceBuilder.setProcTime((ProcTime) obj);
                    break;
                }
            case PROC_TIME :
                state = State.OVERLOAD;
                if (obj instanceof Overload) {
                    metricPceBuilder.setOverload((Overload) obj);
                    break;
                }
            case OVERLOAD :
                state = State.END;
                break;
            case END :
                break;
            }
            if (!state.equals(State.END)) {
                objects.remove(0);
            }
        }
        return metricPceBuilder.build();
    }

    private enum State {
        START, PROC_TIME, OVERLOAD, END;
    }

}

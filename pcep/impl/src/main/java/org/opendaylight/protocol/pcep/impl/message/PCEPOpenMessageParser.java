/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.impl.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;

import org.opendaylight.protocol.pcep.spi.AbstractMessageParser;
import org.opendaylight.protocol.pcep.spi.MessageUtil;
import org.opendaylight.protocol.pcep.spi.ObjectRegistry;
import org.opendaylight.protocol.pcep.spi.PCEPDeserializerException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.message.rev131007.OpenBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.Object;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.OpenMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.open.message.OpenMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.open.object.Open;

/**
 * Parser for {@link OpenMessage}
 */
public class PCEPOpenMessageParser extends AbstractMessageParser {

    public static final int TYPE = 1;

    public PCEPOpenMessageParser(final ObjectRegistry registry) {
        super(registry);
    }

    @Override
    public void serializeMessage(final Message message, final ByteBuf out) {
        if (!(message instanceof OpenMessage)) {
            throw new IllegalArgumentException("Wrong instance of Message. Passed instance " + message.getClass() + ". Needed OpenMessage.");
        }
        final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.open.message.OpenMessage open = ((OpenMessage) message).getOpenMessage();

        if (open.getOpen() == null) {
            throw new IllegalArgumentException("Open Object must be present in Open Message.");
        }
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(serializeObject(open.getOpen()));
        MessageUtil.formatMessage(TYPE, buffer, out);
    }

    @Override
    protected org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.message.rev131007.Open validate(
            final List<Object> objects, final List<Message> errors) throws PCEPDeserializerException {
        if (objects == null) {
            throw new IllegalArgumentException("Passed list can't be null.");
        }

        if (objects.isEmpty() || !(objects.get(0) instanceof Open)) {
            throw new PCEPDeserializerException("Open message doesn't contain OPEN object.");
        }

        final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.open.message.OpenMessage msg = new OpenMessageBuilder().setOpen(
                (Open) objects.get(0)).build();

        objects.remove(0);

        if (!objects.isEmpty()) {
            throw new PCEPDeserializerException("Unprocessed Objects: " + objects);
        }

        return new OpenBuilder().setOpenMessage(msg).build();
    }
}

/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.object;

import org.opendaylight.protocol.concepts.Bandwidth;

/**
 * Structure of specific Bandwidth. Used in Composite Requested Object to refer
 * to bandwidth of existing path.
 */
public class PCEPExistingPathBandwidthObject extends PCEPBandwidthObject {

    /**
     * Constructs Existing Path Bandwidth.
     *
     * @param bandwidth
     *            Bandwidth. Can't be null.
     * @param processed
     *            boolean
     * @param ignored
     *            boolean
     */
    public PCEPExistingPathBandwidthObject(Bandwidth bandwidth, boolean processed, boolean ignored) {
	super(bandwidth, processed, ignored);
    }
}
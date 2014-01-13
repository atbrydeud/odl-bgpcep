/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Generated file

 * Generated from: yang module name: bgp-rib-impl  yang module local name: bgp-dispatcher-impl
 * Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
 * Generated at: Wed Nov 06 13:02:32 CET 2013
 *
 * Do not modify this file unless it is present under src/main directory
 */
package org.opendaylight.controller.config.yang.bgp.rib.impl;

import org.opendaylight.protocol.bgp.parser.spi.BGPExtensionConsumerContext;
import org.opendaylight.protocol.bgp.rib.impl.BGPDispatcherImpl;

/**
 *
 */
public final class BGPDispatcherImplModule
extends
org.opendaylight.controller.config.yang.bgp.rib.impl.AbstractBGPDispatcherImplModule {

	public BGPDispatcherImplModule(
			final org.opendaylight.controller.config.api.ModuleIdentifier name,
			final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
		super(name, dependencyResolver);
	}

	public BGPDispatcherImplModule(
			final org.opendaylight.controller.config.api.ModuleIdentifier name,
			final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
			final BGPDispatcherImplModule oldModule,
			final java.lang.AutoCloseable oldInstance) {
		super(name, dependencyResolver, oldModule, oldInstance);
	}

	@Override
	public void validate() {
		super.validate();
	}

	@Override
	public java.lang.AutoCloseable createInstance() {
		final BGPExtensionConsumerContext bgpExtensions = getBgpExtensionsDependency();
		return new BGPDispatcherImpl(bgpExtensions.getMessageRegistry(),
				getBossGroupDependency(), getWorkerGroupDependency());
	}
}

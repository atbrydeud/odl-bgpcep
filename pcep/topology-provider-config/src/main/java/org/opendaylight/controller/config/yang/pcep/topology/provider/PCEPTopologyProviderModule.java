/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Generated file

 * Generated from: yang module name: config-pcep-topology-provider  yang module local name: pcep-topology-impl
 * Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
 * Generated at: Mon Nov 18 21:08:29 CET 2013
 *
 * Do not modify this file unless it is present under src/main directory
 */
package org.opendaylight.controller.config.yang.pcep.topology.provider;

import com.google.common.base.Charsets;
import com.google.common.net.InetAddresses;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.opendaylight.bgpcep.pcep.topology.provider.PCEPTopologyProvider;
import org.opendaylight.bgpcep.tcpmd5.KeyMapping;
import org.opendaylight.controller.config.api.JmxAttributeValidationException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class PCEPTopologyProviderModule extends
        org.opendaylight.controller.config.yang.pcep.topology.provider.AbstractPCEPTopologyProviderModule {
    private static final Logger LOG = LoggerFactory.getLogger(PCEPTopologyProviderModule.class);

    public PCEPTopologyProviderModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public PCEPTopologyProviderModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, final PCEPTopologyProviderModule oldModule,
            final java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    private KeyMapping contructKeys() {
        final KeyMapping ret = new KeyMapping();
        if (getClient() != null) {
            for (Client c : getClient()) {
                if (c.getAddress() == null) {
                    LOG.warn("Client {} does not have an address skipping it", c);
                    continue;
                }
                if (c.getPassword() != null) {
                    final String s = getAddressString(c.getAddress());
                    ret.put(InetAddresses.forString(s), c.getPassword().getValue().getBytes(Charsets.US_ASCII));
                }
            }
        }
        return ret;
    }

    private String getAddressString(final IpAddress address) {
        if (address.getIpv4Address() != null) {
            return address.getIpv4Address().getValue();
        }
        if (address.getIpv6Address() != null) {
            return address.getIpv6Address().getValue();
        }

        throw new IllegalArgumentException(String.format("Address %s is invalid", address));
    }

    @Override
    public void customValidation() {
        JmxAttributeValidationException.checkNotNull(getTopologyId(), "is not set.", topologyIdJmxAttribute);
        JmxAttributeValidationException.checkNotNull(getListenAddress(), "is not set.", listenAddressJmxAttribute);
        JmxAttributeValidationException.checkNotNull(getListenPort(), "is not set.", listenPortJmxAttribute);
        JmxAttributeValidationException.checkNotNull(getStatefulPlugin(), "is not set.", statefulPluginJmxAttribute);

        final KeyMapping keys = contructKeys();
        if (!keys.isEmpty()) {
            /*
             *  This is a nasty hack, but we don't have another clean solution. We cannot allow
             *  password being set if the injected dispatcher does not have the optional
             *  md5-server-channel-factory set.
             *
             *  FIXME: this is a use case for Module interfaces, e.g. PCEPDispatcherImplModule
             *         should something like isMd5ServerSupported()
             */
            final MBeanServer srv = ManagementFactory.getPlatformMBeanServer();
            Object scf;
            try {
                final ObjectName ci = (ObjectName) srv.getAttribute(getDispatcher(), "CurrentImplementation");

                // FIXME: AbstractPCEPDispatcherImplModule.md5ServerChannelFactoryJmxAttribute.getAttributeName()
                scf = srv.getAttribute(ci, "Md5ServerChannelFactory");
                JmxAttributeValidationException.checkCondition(scf != null, "password is not compatible with selected dispatcher",
                        clientJmxAttribute);
            } catch (AttributeNotFoundException | InstanceNotFoundException | MBeanException | ReflectionException e) {
                JmxAttributeValidationException.wrap(e, "password support could not be validated", clientJmxAttribute);
            }
        }
    }

    private InetAddress listenAddress() {
        final IpAddress a = getListenAddress();
        if (a.getIpv4Address() != null) {
            return InetAddresses.forString(a.getIpv4Address().getValue());
        } else if (a.getIpv6Address() != null) {
            return InetAddresses.forString(a.getIpv6Address().getValue());
        } else {
            throw new IllegalArgumentException("Address " + a + " not supported");
        }
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final InstanceIdentifier<Topology> topology = InstanceIdentifier.builder(NetworkTopology.class).child(Topology.class,
                new TopologyKey(getTopologyId())).toInstance();
        final InetSocketAddress address = new InetSocketAddress(listenAddress(), getListenPort().getValue());
        final KeyMapping keys = contructKeys();

        try {
            return PCEPTopologyProvider.create(getDispatcherDependency(), address, keys.isEmpty() ? null : keys, getSchedulerDependency(),
                    getDataProviderDependency(), getRpcRegistryDependency(), topology, getStatefulPluginDependency());
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Failed to instantiate topology provider at {}", address, e);
            throw new IllegalStateException("Failed to instantiate provider", e);
        }
    }
}

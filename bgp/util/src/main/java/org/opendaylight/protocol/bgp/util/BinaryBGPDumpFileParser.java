/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedBytes;

/**
 * This extracter extracts BGP messages in binary form from a file in MRT format.
 * (http://www.ripe.net/data-tools/stats/ris/ris-raw-data) The parser detects BGP messages by searching for 16 FF bytes,
 * everything else before or after is ignored.
 */
@Immutable
public final class BinaryBGPDumpFileParser {

	private static final byte ff = (byte) 255;
	private static final Logger LOG = LoggerFactory.getLogger(BinaryBGPDumpFileParser.class);
	private static final int MINIMAL_LENGTH = 19;

	/**
	 * Extract BGP messages from binary file in MRT format.
	 * 
	 * @param file file with BGP messages in binary form.
	 * @return list with byte arrays representing extracted messages.
	 * @throws IOException
	 */
	public static List<byte[]> parseMessages(final byte[] byteArray) {

		final List<byte[]> messages = Lists.newLinkedList();
		// search for 16 FFs
		for (int i = 0; i < byteArray.length; i++) {
			final byte b = byteArray[i];

			// Marker start
			if (b == ff) {
				final int start = i;
				int ffCount = 0;
				for (int j = i; j < i + (17); j++) {
					// Check marker
					if (byteArray[j] == ff) {
						ffCount++;
					} else if (ffCount == 16) {
						if (j == (i + 16)) {
							// Parse length
							final int length = UnsignedBytes.toInt(byteArray[j]) * 256 + UnsignedBytes.toInt(byteArray[j + 1]);

							Preconditions.checkArgument(length >= MINIMAL_LENGTH, "Invalid message at index " + start
									+ ", length atribute is lower than " + MINIMAL_LENGTH);

							final byte[] message = Arrays.copyOfRange(byteArray, start, start + length);
							messages.add(message);
							j += length - 16;
						}
						i = j;
						break;
					} else {
						break;
					}
				}
			}

		}
		LOG.info("Succesfully extracted " + messages.size() + " messages");
		return messages;
	}
}
/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.schema;

import java.io.*;

import org.eclipse.pde.internal.core.ischema.*;

public class SchemaEnumeration extends SchemaObject implements ISchemaEnumeration {

	public SchemaEnumeration(ISchemaObject parent, String name) {
		super(parent, name);
	}

	public void write(String indent, PrintWriter writer) {
		writer.println(indent + "<enumeration value=\"" + getName() + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
		String description = getDescription();
		if (description != null)
			description.trim();
		if (description != null && description.length() > 0) {
			String indent2 = indent + Schema.INDENT;
			String indent3 = indent2 + Schema.INDENT;
			writer.println(indent2 + "<annotation>"); //$NON-NLS-1$
			writer.println(indent3 + "<documentation>"); //$NON-NLS-1$
			writer.println(indent3 + description);
			writer.println(indent3 + "</documentation>"); //$NON-NLS-1$
			writer.println(indent2 + "</annotation>"); //$NON-NLS-1$
		}
		writer.println(indent + "</enumeration>"); //$NON-NLS-1$
	}
}

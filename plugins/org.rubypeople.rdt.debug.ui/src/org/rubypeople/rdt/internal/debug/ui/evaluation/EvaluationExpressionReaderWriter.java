/*
 * Author: Markus Barchfeld
 * 
 * Copyright (c) 2005 RubyPeople.
 * 
 * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT is
 * subject to the "Common Public License (CPL) v 1.0". You may not use RDT except in 
 * compliance with the License. For further information see org.rubypeople.rdt/rdt.license.
 * This file is based on org.eclipse.jface.text.templates.persistence.TemplateReaderWriter
 * /

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

package org.rubypeople.rdt.internal.debug.ui.evaluation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jface.text.Assert;
import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Serializes templates as character or byte stream and reads the same format
 * back. Clients may instantiate this class, it is not intended to be
 * subclassed.
 * 
 * @since 3.0
 */
public class EvaluationExpressionReaderWriter {

	private static final String TEMPLATE_ROOT = "expressions"; //$NON-NLS-1$
	private static final String TEMPLATE_ELEMENT = "expression"; //$NON-NLS-1$
	private static final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
	private static final String DESCRIPTION_ATTRIBUTE = "description"; //$NON-NLS-1$
	private static final String ENABLED_ATTRIBUTE = "enabled"; //$NON-NLS-1$

	/**
	 * Create a new instance.
	 */
	public EvaluationExpressionReaderWriter() {}

	/**
	 * Reads templates from a reader and returns them. The reader must present a
	 * serialized form as produced by the <code>save</code> method.
	 * 
	 * @param reader
	 *            the reader to read templates from
	 * @return the read templates, encapsulated in instances of
	 *         <code>TemplatePersistenceData</code>
	 * @throws IOException
	 *             if reading from the stream fails
	 */
	public EvaluationExpression[] read(Reader reader) throws IOException {
		return read(reader, null);
	}

	/**
	 * Reads templates from a stream and adds them to the templates.
	 * 
	 * @param reader
	 *            the reader to read templates from
	 * @param bundle
	 *            a resource bundle to use for translating the read templates,
	 *            or <code>null</code> if no translation should occur
	 * @return the read templates, encapsulated in instances of
	 *         <code>TemplatePersistenceData</code>
	 * @throws IOException
	 *             if reading from the stream fails
	 */
	public EvaluationExpression[] read(Reader reader, ResourceBundle bundle) throws IOException {
		return read(new InputSource(reader), bundle);
	}

	public EvaluationExpression[] read(InputStream stream, ResourceBundle bundle) throws IOException {
		return read(new InputSource(stream), bundle);
	}

	private EvaluationExpression[] read(InputSource source, ResourceBundle bundle) throws IOException {
		try {
			Collection expressions = new ArrayList();
			Set ids = new HashSet();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			Document document = parser.parse(source);

			NodeList elements = document.getElementsByTagName(TEMPLATE_ELEMENT);

			int count = elements.getLength();
			for (int i = 0; i != count; i++) {
				Node node = elements.item(i);
				NamedNodeMap attributes = node.getAttributes();

				if (attributes == null) continue;

				String name = getStringValue(attributes, NAME_ATTRIBUTE);
				name = translateString(name, bundle);

				String description = getStringValue(attributes, DESCRIPTION_ATTRIBUTE, ""); //$NON-NLS-1$
				description = translateString(description, bundle);

				String enabled = getStringValue(attributes, ENABLED_ATTRIBUTE, "false"); //$NON-NLS-1$

				StringBuffer buffer = new StringBuffer();
				NodeList children = node.getChildNodes();
				for (int j = 0; j != children.getLength(); j++) {
					String value = children.item(j).getNodeValue();
					if (value != null) buffer.append(value);
				}
				expressions.add(new EvaluationExpression(name, description, buffer.toString(), new Boolean(enabled)));
			}

			return (EvaluationExpression[]) expressions.toArray(new EvaluationExpression[expressions.size()]);

		} catch (ParserConfigurationException e) {
			Assert.isTrue(false);
		} catch (SAXException e) {
			Throwable t = e.getCause();
			if (t instanceof IOException)
				throw (IOException) t;
			else
				throw new IOException(t.getMessage());
		}

		return null; // dummy
	}

	/**
	 * Saves the templates as XML, encoded as UTF-8 onto the given byte stream.
	 * 
	 * @param templates
	 *            the templates to save
	 * @param stream
	 *            the byte output to write the templates to in XML
	 * @throws IOException
	 *             if writing the templates fails
	 */
	public void save(EvaluationExpression[] templates, OutputStream stream) throws IOException {
		save(templates, new StreamResult(stream));
	}

	/**
	 * Saves the templates as XML.
	 * 
	 * @param templates
	 *            the templates to save
	 * @param writer
	 *            the writer to write the templates to in XML
	 * @throws IOException
	 *             if writing the templates fails
	 */
	public void save(EvaluationExpression[] templates, Writer writer) throws IOException {
		save(templates, new StreamResult(writer));
	}

	/**
	 * Saves the templates as XML.
	 * 
	 * @param templates
	 *            the templates to save
	 * @param result
	 *            the stream result to write to
	 * @throws IOException
	 *             if writing the templates fails
	 */
	private void save(EvaluationExpression[] templates, StreamResult result) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			Node root = document.createElement(TEMPLATE_ROOT); //$NON-NLS-1$
			document.appendChild(root);

			for (int i = 0; i < templates.length; i++) {
				EvaluationExpression evaluationExpression = templates[i];

				Node node = document.createElement(TEMPLATE_ELEMENT);
				root.appendChild(node);

				NamedNodeMap attributes = node.getAttributes();

				Attr name = document.createAttribute(NAME_ATTRIBUTE);
				name.setValue(evaluationExpression.getName());
				attributes.setNamedItem(name);

				Attr description = document.createAttribute(DESCRIPTION_ATTRIBUTE);
				description.setValue(evaluationExpression.getDescription());
				attributes.setNamedItem(description);

				Attr enabled =  document.createAttribute(ENABLED_ATTRIBUTE);
				enabled.setValue(String.valueOf(evaluationExpression.isEnabled()));
				attributes.setNamedItem(enabled);

				Text pattern = document.createTextNode(evaluationExpression.getExpression());
				node.appendChild(pattern);

			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			DOMSource source = new DOMSource(document);

			transformer.transform(source, result);

		} catch (ParserConfigurationException e) {
			Assert.isTrue(false);
		} catch (TransformerException e) {
			if (e.getException() instanceof IOException) throw (IOException) e.getException();
			Assert.isTrue(false);
		}
	}

	private String getStringValue(NamedNodeMap attributes, String name) throws SAXException {
		return getStringValue(attributes, name, null);
	}

	private String getStringValue(NamedNodeMap attributes, String name, String defaultValue) {
		Node node = attributes.getNamedItem(name);
		return node == null ? defaultValue : node.getNodeValue();
	}

	private String translateString(String str, ResourceBundle bundle) {
		if (bundle == null) return str;

		int idx = str.indexOf('%');
		if (idx == -1) { return str; }
		StringBuffer buf = new StringBuffer();
		int k = 0;
		while (idx != -1) {
			buf.append(str.substring(k, idx));
			for (k = idx + 1; k < str.length() && !Character.isWhitespace(str.charAt(k)); k++) {
				// loop
			}
			String key = str.substring(idx + 1, k);
			buf.append(getBundleString(key, bundle));
			idx = str.indexOf('%', k);
		}
		buf.append(str.substring(k));
		return buf.toString();
	}

	private String getBundleString(String key, ResourceBundle bundle) {
		if (bundle != null) {
			try {
				return bundle.getString(key);
			} catch (MissingResourceException e) {
				return '!' + key + '!';
			}
		} else
			return RdtDebugUiMessages.getString(key); // default messages
	}
}


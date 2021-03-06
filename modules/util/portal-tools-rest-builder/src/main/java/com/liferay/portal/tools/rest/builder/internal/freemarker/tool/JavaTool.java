/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.tools.rest.builder.internal.freemarker.tool;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.tools.rest.builder.internal.freemarker.tool.java.JavaParameter;
import com.liferay.portal.tools.rest.builder.internal.freemarker.tool.java.JavaSignature;
import com.liferay.portal.tools.rest.builder.internal.util.CamelCaseUtil;
import com.liferay.portal.vulcan.yaml.config.ConfigYAML;
import com.liferay.portal.vulcan.yaml.openapi.Components;
import com.liferay.portal.vulcan.yaml.openapi.Content;
import com.liferay.portal.vulcan.yaml.openapi.Delete;
import com.liferay.portal.vulcan.yaml.openapi.Get;
import com.liferay.portal.vulcan.yaml.openapi.Head;
import com.liferay.portal.vulcan.yaml.openapi.Items;
import com.liferay.portal.vulcan.yaml.openapi.OpenAPIYAML;
import com.liferay.portal.vulcan.yaml.openapi.Operation;
import com.liferay.portal.vulcan.yaml.openapi.Options;
import com.liferay.portal.vulcan.yaml.openapi.Parameter;
import com.liferay.portal.vulcan.yaml.openapi.PathItem;
import com.liferay.portal.vulcan.yaml.openapi.Post;
import com.liferay.portal.vulcan.yaml.openapi.Put;
import com.liferay.portal.vulcan.yaml.openapi.RequestBody;
import com.liferay.portal.vulcan.yaml.openapi.Response;
import com.liferay.portal.vulcan.yaml.openapi.Schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Peter Shin
 */
public class JavaTool {

	public static JavaTool getInstance() {
		return _instance;
	}

	public Object getComponent(OpenAPIYAML openAPIYAML, String reference) {
		if (reference == null) {
			return null;
		}

		Components components = openAPIYAML.getComponents();
		String referenceType = getComponentType(reference);

		if (reference.startsWith("#/components/parameters/")) {
			Map<String, Parameter> parameters = components.getParameters();

			for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
				if (referenceType.equals(entry.getKey())) {
					return entry.getValue();
				}
			}
		}
		else if (reference.startsWith("#/components/schemas/")) {
			Map<String, Schema> schemas = components.getSchemas();

			for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
				if (referenceType.equals(entry.getKey())) {
					return entry.getValue();
				}
			}
		}

		return null;
	}

	public String getComponentType(String reference) {
		int index = reference.lastIndexOf('/');

		if (index == -1) {
			return reference;
		}

		return reference.substring(index + 1);
	}

	public String getHTTPMethod(Operation operation) {
		if (Delete.class.isInstance(operation)) {
			return "delete";
		}
		else if (Get.class.isInstance(operation)) {
			return "get";
		}
		else if (Head.class.isInstance(operation)) {
			return "head";
		}
		else if (Options.class.isInstance(operation)) {
			return "options";
		}
		else if (Post.class.isInstance(operation)) {
			return "post";
		}
		else if (Put.class.isInstance(operation)) {
			return "put";
		}

		return null;
	}

	public JavaParameter getJavaParameter(Parameter parameter) {
		List<String> parameterAnnotations = new ArrayList<>();

		Schema schema = parameter.getSchema();

		if (schema.getType() != null) {
			StringBuilder sb = new StringBuilder();

			sb.append("@");
			sb.append(StringUtil.upperCaseFirstLetter(parameter.getIn()));
			sb.append("Param(\"");
			sb.append(parameter.getName());
			sb.append("\")");

			parameterAnnotations.add(sb.toString());
		}

		String parameterName = CamelCaseUtil.toCamelCase(
			parameter.getName(), false);
		String parameterType = _getJavaParameterType(null, schema);

		return new JavaParameter(
			parameterAnnotations, parameterName, parameterType);
	}

	public JavaParameter getJavaParameter(
		String propertySchemaName, Schema schema) {

		String parameterName = CamelCaseUtil.toCamelCase(
			propertySchemaName, false);
		String parameterType = _getJavaParameterType(
			propertySchemaName, schema);

		return new JavaParameter(null, parameterName, parameterType);
	}

	public JavaSignature getJavaSignature(
		ConfigYAML configYAML, OpenAPIYAML openAPIYAML, Operation operation,
		String path, PathItem pathItem, String schemaName) {

		String returnType = _getReturnType(openAPIYAML, operation);

		return new JavaSignature(
			_getJavaParameters(operation, returnType, schemaName),
			_getMethodAnnotations(operation, pathItem, path),
			_getMethodName(operation, path, returnType, schemaName),
			returnType);
	}

	public List<String> getMediaTypes(Map<String, Content> contents) {
		if ((contents == null) || contents.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> mediaTypes = new ArrayList<>(contents.keySet());

		Collections.sort(mediaTypes);

		return mediaTypes;
	}

	public List<Operation> getOperations(PathItem pathItem) {
		List<Operation> operations = new ArrayList<>();

		if (pathItem.getDelete() != null) {
			operations.add(pathItem.getDelete());
		}

		if (pathItem.getGet() != null) {
			operations.add(pathItem.getGet());
		}

		if (pathItem.getHead() != null) {
			operations.add(pathItem.getHead());
		}

		if (pathItem.getOptions() != null) {
			operations.add(pathItem.getOptions());
		}

		if (pathItem.getPost() != null) {
			operations.add(pathItem.getPost());
		}

		if (pathItem.getPut() != null) {
			operations.add(pathItem.getPut());
		}

		return operations;
	}

	private JavaTool() {
	}

	private String _getJavaDataType(
		String type, String format, String propertySchemaName) {

		if (StringUtil.equals(format, "date-time") &&
			StringUtil.equals(type, "string")) {

			return "Date";
		}

		if (StringUtil.equals(format, "int64") &&
			StringUtil.equals(type, "integer")) {

			return "Long";
		}

		if (StringUtil.equalsIgnoreCase(type, "object") &&
			(propertySchemaName != null)) {

			return StringUtil.upperCaseFirstLetter(propertySchemaName);
		}

		return StringUtil.upperCaseFirstLetter(type);
	}

	private List<JavaParameter> _getJavaParameters(
		Operation operation, String returnType, String schemaName) {

		if ((operation == null) || (operation.getParameters() == null)) {
			return Collections.emptyList();
		}

		List<JavaParameter> javaParameters = new ArrayList<>();

		List<Parameter> parameters = operation.getParameters();

		Set<String> parameterNames = new HashSet<>();

		for (Parameter parameter : parameters) {
			parameterNames.add(parameter.getName());
		}

		for (Parameter parameter : parameters) {
			String parameterName = parameter.getName();

			if (StringUtil.equals(parameterName, "Accept-Language") ||
				StringUtil.equals(parameterName, "filter") ||
				StringUtil.equals(parameterName, "sort")) {

				continue;
			}

			if (StringUtil.equals(parameterName, "page") ||
				StringUtil.equals(parameterName, "per_page")) {

				if (parameterNames.contains("page") &&
					parameterNames.contains("per_page")) {

					continue;
				}
			}

			javaParameters.add(getJavaParameter(parameter));
		}

		if (parameterNames.contains("filter")) {
			JavaParameter javaParameter = new JavaParameter(
				Collections.singletonList("@Context"), "filter", "Filter");

			javaParameters.add(javaParameter);
		}

		if (parameterNames.contains("page") &&
			parameterNames.contains("per_page")) {

			JavaParameter javaParameter = new JavaParameter(
				Collections.singletonList("@Context"), "pagination",
				"Pagination");

			javaParameters.add(javaParameter);
		}

		if (parameterNames.contains("sort")) {
			JavaParameter javaParameter = new JavaParameter(
				Collections.singletonList("@Context"), "sorts", "Sort[]");

			javaParameters.add(javaParameter);
		}

		String httpMethod = getHTTPMethod(operation);

		if ((Objects.equals(httpMethod, "post") ||
			 Objects.equals(httpMethod, "put")) &&
			Objects.equals(returnType, schemaName)) {

			String parameterName = StringUtil.lowerCaseFirstLetter(schemaName);

			javaParameters.add(
				new JavaParameter(null, parameterName, schemaName));
		}

		return javaParameters;
	}

	private String _getJavaParameterType(
		String propertySchemaName, Schema schema) {

		Items items = schema.getItems();
		String type = schema.getType();

		if (StringUtil.equals(type, "array") && (items != null)) {
			if (items.getType() != null) {
				String itemsFormat = items.getFormat();
				String itemsType = items.getType();

				String javaDataType = _getJavaDataType(
					itemsType, itemsFormat, propertySchemaName);

				return javaDataType + "[]";
			}

			if (items.getReference() != null) {
				return getComponentType(items.getReference()) + "[]";
			}
		}

		if (type != null) {
			return _getJavaDataType(
				type, schema.getFormat(), propertySchemaName);
		}

		List<Schema> allOfSchemas = schema.getAllOfSchemas();

		if (allOfSchemas != null) {
			for (Schema allOfSchema : allOfSchemas) {
				if (Validator.isNotNull(allOfSchema.getReference())) {
					return getComponentType(allOfSchema.getReference());
				}
			}
		}

		if ((schema.getAnyOfSchemas() != null) ||
			(schema.getOneOfSchemas() != null)) {

			return "Object";
		}

		return getComponentType(schema.getReference());
	}

	private String _getMethodAnnotationConsumes(Operation operation) {
		RequestBody requestBody = operation.getRequestBody();

		if (requestBody == null) {
			return null;
		}

		Map<String, Content> contents = requestBody.getContent();

		List<String> mediaTypes = getMediaTypes(contents);

		if (mediaTypes.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < mediaTypes.size(); i++) {
			sb.append(StringUtil.quote(mediaTypes.get(i), "\""));

			if (i < (mediaTypes.size() - 1)) {
				sb.append(",");
			}
		}

		if (mediaTypes.size() > 1) {
			return "@Consumes({" + sb.toString() + "})";
		}

		return "@Consumes(" + sb.toString() + ")";
	}

	private String _getMethodAnnotationProduces(Operation operation) {
		Map<String, Response> responses = operation.getResponses();

		if (responses.isEmpty()) {
			return null;
		}

		List<String> mediaTypes = new ArrayList<>();

		for (Response response : responses.values()) {
			mediaTypes.addAll(getMediaTypes(response.getContent()));
		}

		if (mediaTypes.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < mediaTypes.size(); i++) {
			sb.append(StringUtil.quote(mediaTypes.get(i), "\""));

			if (i < (mediaTypes.size() - 1)) {
				sb.append(",");
			}
		}

		if (mediaTypes.size() > 1) {
			return "@Produces({" + sb.toString() + "})";
		}

		return "@Produces(" + sb.toString() + ")";
	}

	private List<String> _getMethodAnnotations(
		Operation operation, PathItem pathItem, String path) {

		List<String> methodAnnotations = new ArrayList<>();

		methodAnnotations.add("@Path(\"" + path + "\")");

		String httpMethod = getHTTPMethod(operation);

		methodAnnotations.add("@" + StringUtil.toUpperCase(httpMethod));

		if (pathItem.getGet() != null) {
			methodAnnotations.add("@RequiresScope(\"everything.read\")");
		}
		else {
			methodAnnotations.add("@RequiresScope(\"everything.write\")");
		}

		String methodAnnotation = _getMethodAnnotationConsumes(operation);

		if (Validator.isNotNull(methodAnnotation)) {
			methodAnnotations.add(methodAnnotation);
		}

		methodAnnotation = _getMethodAnnotationProduces(operation);

		if (Validator.isNotNull(methodAnnotation)) {
			methodAnnotations.add(methodAnnotation);
		}

		return methodAnnotations;
	}

	private String _getMethodName(
		Operation operation, String path, String returnType,
		String schemaName) {

		String httpMethod = getHTTPMethod(operation);

		String s = path.replaceAll("(?s)\\{.*?\\}", "");

		String name = httpMethod + CamelCaseUtil.toCamelCase(s, true);

		if (StringUtil.startsWith(returnType, "Page<") &&
			!StringUtil.endsWith(name, "Page")) {

			return name + "Page";
		}

		if (StringUtil.equals(returnType, schemaName) &&
			StringUtil.endsWith(name, schemaName + "s")) {

			return name.substring(0, name.length() - 1);
		}

		return name;
	}

	private String _getReturnType(
		OpenAPIYAML openAPIYAML, Operation operation) {

		Map<String, Response> responses = operation.getResponses();

		if (responses.isEmpty()) {
			return "Response";
		}

		for (Response response : responses.values()) {
			Map<String, Content> contents = response.getContent();

			if ((contents == null) || (contents.values() == null)) {
				continue;
			}

			for (Content content : contents.values()) {
				Schema schema = content.getSchema();

				if (schema == null) {
					continue;
				}

				String javaParameterType = _getJavaParameterType(null, schema);

				if (javaParameterType.endsWith("[]")) {
					String s = javaParameterType.substring(
						0, javaParameterType.length() - 2);

					return "Page<" + s + ">";
				}

				Object component = getComponent(
					openAPIYAML, schema.getReference());

				if (component instanceof Schema) {
					return getComponentType(schema.getReference());
				}
			}
		}

		return "Response";
	}

	private static JavaTool _instance = new JavaTool();

}
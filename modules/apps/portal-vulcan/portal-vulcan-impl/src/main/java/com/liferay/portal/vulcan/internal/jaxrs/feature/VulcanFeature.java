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

package com.liferay.portal.vulcan.internal.jaxrs.feature;

import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.odata.filter.ExpressionConvert;
import com.liferay.portal.vulcan.internal.jaxrs.context.provider.AcceptLanguageContextProvider;
import com.liferay.portal.vulcan.internal.jaxrs.context.provider.CompanyContextProvider;
import com.liferay.portal.vulcan.internal.jaxrs.context.provider.FilterContextProvider;
import com.liferay.portal.vulcan.internal.jaxrs.context.provider.PaginationContextProvider;
import com.liferay.portal.vulcan.internal.jaxrs.context.provider.SortContextProvider;
import com.liferay.portal.vulcan.internal.jaxrs.exception.mapper.NoSuchModelExceptionMapper;
import com.liferay.portal.vulcan.internal.jaxrs.exception.mapper.PortalExceptionMapper;
import com.liferay.portal.vulcan.internal.jaxrs.exception.mapper.PrincipalExceptionMapper;
import com.liferay.portal.vulcan.internal.jaxrs.json.JSONMessageBodyReader;
import com.liferay.portal.vulcan.internal.jaxrs.json.JSONMessageBodyWriter;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

/**
 * An {@code Application} requesting this feature will include all the different
 * extensions provided by this module.
 *
 * @author Alejandro Hernández
 * @review
 */
@Component(
	property = {
		JaxrsWhiteboardConstants.JAX_RS_APPLICATION_SELECT + "=(osgi.jaxrs.extension.select=\\(osgi.jaxrs.name=Liferay.Vulcan\\))",
		JaxrsWhiteboardConstants.JAX_RS_EXTENSION + "=true",
		JaxrsWhiteboardConstants.JAX_RS_NAME + "=Liferay.Vulcan"
	},
	scope = ServiceScope.PROTOTYPE, service = Feature.class
)
public class VulcanFeature implements Feature {

	@Override
	public boolean configure(FeatureContext featureContext) {
		featureContext.register(JSONMessageBodyReader.class);
		featureContext.register(JSONMessageBodyWriter.class);
		featureContext.register(NoSuchModelExceptionMapper.class);
		featureContext.register(PaginationContextProvider.class);
		featureContext.register(PortalExceptionMapper.class);
		featureContext.register(PrincipalExceptionMapper.class);

		featureContext.register(new AcceptLanguageContextProvider(_portal));
		featureContext.register(new CompanyContextProvider(_portal));
		featureContext.register(
			new FilterContextProvider(
				_bundleContext, _expressionConvert, _portal));
		featureContext.register(
			new SortContextProvider(_bundleContext, _portal));

		return false;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	private BundleContext _bundleContext;

	@Reference(
		target = "(result.class.name=com.liferay.portal.kernel.search.filter.Filter)"
	)
	private ExpressionConvert<Filter> _expressionConvert;

	@Reference
	private Portal _portal;

}
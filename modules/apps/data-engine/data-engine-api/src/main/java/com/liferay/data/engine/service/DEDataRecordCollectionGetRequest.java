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

package com.liferay.data.engine.service;

/**
* This class represents a request to get a {@link DEDataRecordCollection} from the database.
*
* @author Leonardo Barros
* @review
*/
public final class DEDataRecordCollectionGetRequest {

	/**
	 * Returns the Data Record Collection ID of the Get request.
	 * @return deDataRecordCollectionId
	 * @review
	 */
	public long getDEDataRecordCollectionId() {
		return _deDataRecordCollectionId;
	}

	/**
	 * Constructs the Get Data Record Collections request using parameters. A
	 * Data Record Collection ID can be used as an argument.
	 * @review
	 */
	public static final class Builder {

		/**
		 * Constructs the Get Data Record Collections request.
		 * @return {@link DEDataRecordCollectionGetRequest }
		 * @review
		 */
		public DEDataRecordCollectionGetRequest build() {
			return _deDataRecordCollectionGetRequest;
		}

		/**
		 * Includes a Data Record Collection ID in the Get request.
		 * @param deDataRecordCollectionId ID from the Data Record
		 * Collection to be retrieved
		 * @return {@link Builder }
		 * @review
		 */
		public Builder byId(long deDataRecordCollectionId) {
			_deDataRecordCollectionGetRequest._deDataRecordCollectionId =
				deDataRecordCollectionId;

			return this;
		}

		private final DEDataRecordCollectionGetRequest
			_deDataRecordCollectionGetRequest =
				new DEDataRecordCollectionGetRequest();

	}

	private DEDataRecordCollectionGetRequest() {
	}

	private long _deDataRecordCollectionId;

}
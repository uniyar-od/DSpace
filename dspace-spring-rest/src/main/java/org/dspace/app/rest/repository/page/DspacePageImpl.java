/*
 * Copyright 2008-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dspace.app.rest.repository.page;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Basic {@code Page} implementation.
 * Original code:
 * 	@See {@link org.springframework.data.domain.PageImpl}
 * 	Version 1.13.x, spring-data-commons.
 * 
 * @param <T> the type of which the page consists.
 */
public class DspacePageImpl <T> extends DspaceChunk<T> implements Page<T> {

	private static final long serialVersionUID = 7128324264563273472L;
	private long total;
	private Pageable pageable;

	/**
	 * Constructor of {@code PageImpl}.
	 * 
	 * @param content the content of this page, must not be {@literal null}.
	 * @param pageable the paging information, can be {@literal null}.
	 * @param total the total amount of items available. The total might be adapted considering the length of the content
	 *          given, if it is going to be the content of the last page. This is in place to mitigate inconsistencies
	 */
	public DspacePageImpl(List<T> content, Pageable pageable, long total) {

		super(content, pageable);
		
		init(pageable, total, true);
	}
	
	/***
	 * Init method, localize constructor logic.
	 * 
	 * @param pageable pageable the paging information, can be {@literal null}.
	 * @param total the total amount of items available. The total might be adapted considering the length of the content
	 *          given, if it is going to be the content of the last page. This is in place to mitigate inconsistencies
	 * @param fullChunk usually set to true if total is equal to this.content.size()
	 */
	void init(Pageable pageable, long total, boolean fullChunk) {
		// fix total
		//this.total = !content.isEmpty() && pageable != null && pageable.getOffset() + pageable.getPageSize() > total
		//		? pageable.getOffset() + content.size() : total;
		this.total = (fullChunk) ? this.content.size() : total;
		
		// fix content
		if (pageable != null && fullChunk) {
			if (pageable.getPageNumber()*pageable.getPageSize() < this.content.size()) {
				int i = 0;
			
				while(i < pageable.getPageNumber()*pageable.getPageSize()) {
					this.content.remove(0);
					i++;
				}
				i = pageable.getPageSize();
				while(i < this.content.size())
					this.content.remove(i);
			}
			else 
				this.content = new ArrayList<T>();
		}
		this.pageable = pageable;
	}
	
	/**
	 * Constructor of {@code PageImpl}.
	 * 
	 * @param content the content of this page, must not be {@literal null}.
	 * @param pageable the paging information, can be {@literal null}.
	 * @param total the total amount of items available. The total might be adapted considering the length of the content
	 *          given, if it is going to be the content of the last page. This is in place to mitigate inconsistencies
	 */
	public DspacePageImpl(List<T> content, Pageable pageable, long total, boolean workaround) {
		super(content, pageable);
		
		init(pageable, total, workaround);
	}
	

	/**
	 * Creates a new {@link PageImpl} with the given content. This will result in the created {@link Page} being identical
	 * to the entire {@link List}.
	 * 
	 * @param content must not be {@literal null}.
	 */
	public DspacePageImpl(List<T> content) {
		this(content, null, null == content ? 0 : content.size());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.domain.Page#getTotalPages()
	 */
	@Override
	public int getTotalPages() {
		return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.domain.Page#getTotalElements()
	 */
	@Override
	public long getTotalElements() {
		return total;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.domain.Slice#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return getNumber() + 1 < getTotalPages();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.domain.Slice#isLast()
	 */
	@Override
	public boolean isLast() {
		return !hasNext();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.domain.Slice#transform(org.springframework.core.convert.converter.Converter)
	 */
	@Override
	public <S> Page<S> map(Converter<? super T, ? extends S> converter) {
		return new DspacePageImpl<S>(getConvertedContent(converter), pageable, total, false);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		String contentType = "UNKNOWN";
		List<T> content = getContent();

		if (content.size() > 0) {
			contentType = content.get(0).getClass().getName();
		}

		return String.format("Page %s of %d containing %s instances", getNumber() + 1, getTotalPages(), contentType);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof DspacePageImpl<?>)) {
			return false;
		}

		DspacePageImpl<?> that = (DspacePageImpl<?>) obj;

		return this.total == that.total && super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = 17;

		result += 31 * (int) (total ^ total >>> 32);
		result += 31 * super.hashCode();

		return result;
	}
}
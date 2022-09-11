package com.neo.util.framework.elastic.impl;

import com.neo.util.common.impl.enumeration.Synchronization;
import com.neo.util.framework.api.persistence.search.*;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ElasticSearchRepositoryIT extends AbstractElasticIntegrationTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRepositoryIT.class);

	private static final String INDEX_NAME_FOR_QUERY = BasicSearchableImpl.INDEX_NAME + "-*";
	private static final String FULL_INDEX_NAME_FOR_QUERY = BasicSearchableImpl.INDEX_NAME + "-no-date-v1";

	@Test
	public void indexSearchableTest() {
		client().admin().indices().prepareDelete(INDEX_NAME_FOR_QUERY).get();
		flushAndRefresh(INDEX_NAME_FOR_QUERY);
		assertFalse(client().admin().indices().prepareExists(INDEX_NAME_FOR_QUERY).get().isExists());

		String uuid = UUID.randomUUID().toString();
		Searchable dummySearchable = getBasicSearchable(uuid);
		elasticSearchRepository.index(dummySearchable, new IndexParameter(Synchronization.SYNCHRONOUS));

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, true);
		assertTrue(client().admin().indices().prepareExists(INDEX_NAME_FOR_QUERY).get().isExists());
	}

	@Test
	public void indexAsynchronousTest() {

		String uuid = UUID.randomUUID().toString();
		Searchable dummySearchable = getBasicSearchable(uuid);
		elasticSearchRepository.index(dummySearchable);

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, true);
	}

	@Test
	public void indexSynchronousTest() {

		String uuid = UUID.randomUUID().toString();
		Searchable dummySearchable = getBasicSearchable(uuid);
		elasticSearchRepository.index(dummySearchable, new IndexParameter(Synchronization.SYNCHRONOUS));

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, true);
	}

	@Test
	public void indexBulkAsynchronousTest() {

		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		Searchable dummySearchable1 = getBasicSearchable(uuid1);
		Searchable dummySearchable2 = getBasicSearchable(uuid2);
		elasticSearchRepository.index(List.of(dummySearchable1, dummySearchable2));

		validateDocumentInIndex(uuid1, INDEX_NAME_FOR_QUERY, true);
		validateDocumentInIndex(uuid2, INDEX_NAME_FOR_QUERY, true);
	}

	@Test
	public void indexBulkSynchronousTest() {

		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		Searchable dummySearchable1 = getBasicSearchable(uuid1);
		Searchable dummySearchable2 = getBasicSearchable(uuid2);
		elasticSearchRepository.index(List.of(dummySearchable1, dummySearchable2),
				new IndexParameter(Synchronization.SYNCHRONOUS));


		validateDocumentInIndex(uuid1, INDEX_NAME_FOR_QUERY, true);
		validateDocumentInIndex(uuid2, INDEX_NAME_FOR_QUERY, true);
	}

	@Test
	public void bulkAsynchronousErrorRetryTest() throws IOException {
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		String uuid3 = UUID.randomUUID().toString();
		Searchable dummySearchable1 = getBasicSearchable(uuid1);
		Searchable dummySearchable2 = getBasicSearchable(uuid2);
		indexBasicSearchable(uuid3);

		closeIndex(FULL_INDEX_NAME_FOR_QUERY);

		elasticSearchRepository.index(List.of(dummySearchable1, dummySearchable2), new IndexParameter());
		elasticSearchRepository.getBulkProcessor().flush();

		openIndex(FULL_INDEX_NAME_FOR_QUERY);

		validateDocumentInIndex(uuid1, INDEX_NAME_FOR_QUERY, false);
		validateDocumentInIndex(uuid3, INDEX_NAME_FOR_QUERY, true);
	}

	@Test
	public void bulkSynchronousErrorRetryTest() throws IOException {
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		String uuid3 = UUID.randomUUID().toString();
		Searchable dummySearchable1 = getBasicSearchable(uuid1);
		Searchable dummySearchable2 = getBasicSearchable(uuid2);
		indexBasicSearchable(uuid3);

		closeIndex(FULL_INDEX_NAME_FOR_QUERY);

		elasticSearchRepository.index(List.of(dummySearchable1, dummySearchable2), new IndexParameter(Synchronization.SYNCHRONOUS));
		elasticSearchRepository.getBulkProcessor().flush();

		openIndex(FULL_INDEX_NAME_FOR_QUERY);

		validateDocumentInIndex(uuid1, INDEX_NAME_FOR_QUERY, false);
		validateDocumentInIndex(uuid3, INDEX_NAME_FOR_QUERY, true);
	}

	@Test
	public void bulkAsynchronousErrorNoRetryTest() {
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		BasicSearchableImpl dummySearchable1 = indexBasicSearchable(uuid1);
		Searchable dummySearchable2 = getBasicSearchable(uuid2);

		elasticSearchRepository.index(List.of(dummySearchable1, dummySearchable2), new IndexParameter());

		validateDocumentInIndex(uuid1, INDEX_NAME_FOR_QUERY, BasicSearchableImpl.FIELD_NAME_TEXT_FIELD, BasicSearchableImpl.TEXT_FIELD_VALUE);
		validateDocumentInIndex(uuid2, INDEX_NAME_FOR_QUERY, true);
	}

	@Test
	public void bulkSynchronousErrorNoRetryTest() {
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		BasicSearchableImpl dummySearchable1 = indexBasicSearchable(uuid1);
		Searchable dummySearchable2 = getBasicSearchable(uuid2);

		elasticSearchRepository.index(List.of(dummySearchable1, dummySearchable2), new IndexParameter(Synchronization.SYNCHRONOUS));

		validateDocumentInIndex(uuid1, INDEX_NAME_FOR_QUERY, BasicSearchableImpl.FIELD_NAME_TEXT_FIELD, BasicSearchableImpl.TEXT_FIELD_VALUE);
		validateDocumentInIndex(uuid2, INDEX_NAME_FOR_QUERY, true);
	}

	/*
	Test Queue
	 */

	@Test
	public void incomingQueueIndexTest() {
		String uuid = UUID.randomUUID().toString();
		Searchable dummySearchable = getBasicSearchable(uuid);
		IndexRequest indexRequest = elasticSearchRepository.generateIndexRequest(dummySearchable);
		QueueableSearchable transportSearchable = elasticSearchRepository.generateQueueableSearchable(indexRequest);

		elasticSearchRepository.process(transportSearchable);

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, true);
	}

	@Test
	public void incomingQueueBulkTest() {
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		String uuid3 = UUID.randomUUID().toString();
		BasicSearchableImpl dummySearchable2 = getBasicSearchable(uuid2);
		BasicSearchableImpl dummySearchable3 = getBasicSearchable(uuid3);
		dummySearchable3.setTextField(uuid3);

		elasticSearchRepository.index(List.of(dummySearchable2, dummySearchable3));

		validateDocumentInIndex(uuid3, INDEX_NAME_FOR_QUERY, true);

		Searchable dummySearchable1 = getBasicSearchable(uuid1);
		IndexRequest indexRequest1 = elasticSearchRepository.generateIndexRequest(dummySearchable1);
		QueueableSearchable transportSearchable1 = elasticSearchRepository.generateQueueableSearchable(indexRequest1);

		List<QueueableSearchable> bulk = List.of(transportSearchable1);

		elasticSearchRepository.process(bulk);

		validateDocumentInIndex(uuid1, INDEX_NAME_FOR_QUERY, true);
		validateDocumentInIndex(uuid2, INDEX_NAME_FOR_QUERY, BasicSearchableImpl.FIELD_NAME_TEXT_FIELD, BasicSearchableImpl.TEXT_FIELD_VALUE);
	}

	/*
	Test Error Cases
	 */

	@Test
	public void indexAsynchronousErrorRetryTest() throws Exception {
		String uuidNotUsed = UUID.randomUUID().toString();
		indexBasicSearchable(uuidNotUsed);

		String uuid = UUID.randomUUID().toString();
		Searchable dummySearchable = getBasicSearchable(uuid);

		closeIndex(FULL_INDEX_NAME_FOR_QUERY);

		elasticSearchRepository.index(dummySearchable, new IndexParameter());
		elasticSearchRepository.getBulkProcessor().flush();

		openIndex(FULL_INDEX_NAME_FOR_QUERY);

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, false);
	}

	@Test
	public void indexAsynchronousErrorNoRetryTest() {
		String uuid = UUID.randomUUID().toString();
		BasicSearchableImpl dummySearchable = indexBasicSearchable(uuid);

		elasticSearchRepository.index(dummySearchable, new IndexParameter());

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, BasicSearchableImpl.FIELD_NAME_TEXT_FIELD, BasicSearchableImpl.TEXT_FIELD_VALUE);
	}

	@Test
	public void indexSynchronousErrorRetryTest() throws Exception {
		String uuidNotUsed = UUID.randomUUID().toString();
		indexBasicSearchable(uuidNotUsed);

		String uuid = UUID.randomUUID().toString();
		Searchable dummySearchable = getBasicSearchable(uuid);

		closeIndex(FULL_INDEX_NAME_FOR_QUERY);

		try {
			elasticSearchRepository.index(dummySearchable, new IndexParameter(Synchronization.SYNCHRONOUS));
			Assert.fail("Expected Exception not thrown!");
		} catch (Exception e) {
			Assert.assertEquals(ElasticsearchStatusException.class, e.getClass());
		} finally {
			openIndex(FULL_INDEX_NAME_FOR_QUERY);
		}

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, false);
	}

	@Test
	public void indexSynchronousErrorNoRetryTest() {
		String uuid = UUID.randomUUID().toString();
		BasicSearchableImpl dummySearchable = indexBasicSearchable(uuid);

		try {
			elasticSearchRepository.index(dummySearchable, new IndexParameter(Synchronization.SYNCHRONOUS));
		} catch (Exception e) {
			Assert.assertFalse(elasticSearchRepository.exceptionIsToBeRetried(e));
		}

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, BasicSearchableImpl.FIELD_NAME_TEXT_FIELD, BasicSearchableImpl.TEXT_FIELD_VALUE);
	}

	@Test
	public void fetchSearchableFromIndexTest() {
		String uuid = UUID.randomUUID().toString();
		BasicSearchableImpl dummySearchable = indexBasicSearchable(uuid);

		String indexName = dummySearchable.getIndexName().concat("-*");
		SearchQuery query = new SearchQuery();
		query.setFields(List.of(BasicSearchableImpl.FIELD_NAME_TEXT_FIELD));

		LOGGER.info("---check--- {}, {}, {}", elasticSearchRepository, indexName, query);
		SearchResult result = elasticSearchRepository.fetch(indexName, query);
		Assert.assertTrue("At least one record expected, SearchResult:" + result, result.getHits().size() >= 1);
	}

	@Test
	public void readTypeMappingTest() {
		String uuid = UUID.randomUUID().toString();
		BasicSearchableImpl dummySearchable = indexBasicSearchable(uuid);

		String indexName = dummySearchable.getIndexName().concat("-*");
		Map<String, Object> mapping = elasticSearchRepository.readTypeMapping(indexName);
		Assert.assertFalse(mapping.isEmpty());

		Map<String, Class<?>> flatMapping = elasticSearchRepository.getFlatTypeMapping(mapping);
		Assert.assertFalse(flatMapping.isEmpty());
		Assert.assertTrue(flatMapping.containsKey(BasicSearchableImpl.FIELD_NAME_TEXT_FIELD)
				&& String.class.equals(flatMapping.get(BasicSearchableImpl.FIELD_NAME_TEXT_FIELD)));
	}

	/*
	Test Helper Methods
	 */

	private BasicSearchableImpl indexBasicSearchable(String uuid) {
		BasicSearchableImpl dummySearchable = getBasicSearchable(uuid);
		elasticSearchRepository.index(dummySearchable);

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, true);

		return dummySearchable;
	}

	private BasicSearchableImpl getBasicSearchable(String uuid) {
		BasicSearchableImpl dummySearchable = new BasicSearchableImpl();
		dummySearchable.setBusinessId(uuid);
		return dummySearchable;
	}
}
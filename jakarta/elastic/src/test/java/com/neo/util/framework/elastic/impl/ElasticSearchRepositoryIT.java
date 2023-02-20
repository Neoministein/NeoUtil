package com.neo.util.framework.elastic.impl;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.neo.util.common.impl.enumeration.Synchronization;
import com.neo.util.framework.api.persistence.search.*;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ElasticSearchRepositoryIT extends AbstractElasticIntegrationTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRepositoryIT.class);

	private static final String INDEX_NAME_FOR_QUERY = BasicPersonSearchable.INDEX_NAME + "-*";
	private static final String FULL_INDEX_NAME_FOR_QUERY = BasicPersonSearchable.INDEX_NAME + "-no-date-v1";

	@Test
	public void indexSearchableTest() {
		client().admin().indices().prepareDelete(INDEX_NAME_FOR_QUERY).get();
		flushAndRefresh(INDEX_NAME_FOR_QUERY);
		assertFalse(checkIfIndexExists(INDEX_NAME_FOR_QUERY));

		String uuid = UUID.randomUUID().toString();
		Searchable dummySearchable = getBasicSearchable(uuid);
		elasticSearchRepository.index(dummySearchable, new IndexParameter(Synchronization.SYNCHRONOUS));

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, true);
		assertFalse(checkIfIndexExists(INDEX_NAME_FOR_QUERY));
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
		elasticSearchRepository.getBulkIngester().flush();

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
		elasticSearchRepository.getBulkIngester().flush();

		openIndex(FULL_INDEX_NAME_FOR_QUERY);

		validateDocumentInIndex(uuid1, INDEX_NAME_FOR_QUERY, false);
		validateDocumentInIndex(uuid3, INDEX_NAME_FOR_QUERY, true);
	}

	@Test
	public void bulkAsynchronousErrorNoRetryTest() {
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		BasicPersonSearchable dummySearchable1 = indexBasicSearchable(uuid1);
		Searchable dummySearchable2 = getBasicSearchable(uuid2);

		elasticSearchRepository.index(List.of(dummySearchable1, dummySearchable2), new IndexParameter());

		validateDocumentInIndex(uuid1, INDEX_NAME_FOR_QUERY, BasicPersonSearchable.F_NAME, BasicPersonSearchable.NAME_VALUE);
		validateDocumentInIndex(uuid2, INDEX_NAME_FOR_QUERY, true);
	}

	@Test
	public void bulkSynchronousErrorNoRetryTest() {
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		BasicPersonSearchable dummySearchable1 = indexBasicSearchable(uuid1);
		Searchable dummySearchable2 = getBasicSearchable(uuid2);

		elasticSearchRepository.index(List.of(dummySearchable1, dummySearchable2), new IndexParameter(Synchronization.SYNCHRONOUS));

		validateDocumentInIndex(uuid1, INDEX_NAME_FOR_QUERY, BasicPersonSearchable.F_NAME, BasicPersonSearchable.NAME_VALUE);
		validateDocumentInIndex(uuid2, INDEX_NAME_FOR_QUERY, true);
	}

	/*
	Test Queue
	 */

	@Test
	public void incomingQueueIndexTest() {
		String uuid = UUID.randomUUID().toString();
		Searchable dummySearchable = getBasicSearchable(uuid);
		BulkOperation indexRequest = elasticSearchRepository.buildIndexOperation(dummySearchable);
		QueueableSearchable transportSearchable = elasticSearchRepository.buildQueueableSearchable(indexRequest);

		elasticSearchRepository.process(transportSearchable);

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, true);
	}

	@Test
	public void incomingQueueBulkTest() {
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		String uuid3 = UUID.randomUUID().toString();
		BasicPersonSearchable dummySearchable2 = getBasicSearchable(uuid2);
		BasicPersonSearchable dummySearchable3 = getBasicSearchable(uuid3);
		dummySearchable3.setName(uuid3);

		elasticSearchRepository.index(List.of(dummySearchable2, dummySearchable3));

		validateDocumentInIndex(uuid3, INDEX_NAME_FOR_QUERY, true);

		Searchable dummySearchable1 = getBasicSearchable(uuid1);
		BulkOperation indexRequest1 = elasticSearchRepository.buildIndexOperation(dummySearchable1);
		QueueableSearchable transportSearchable1 = elasticSearchRepository.buildQueueableSearchable(indexRequest1);

		List<QueueableSearchable> bulk = List.of(transportSearchable1);

		elasticSearchRepository.process(bulk);

		validateDocumentInIndex(uuid1, INDEX_NAME_FOR_QUERY, true);
		validateDocumentInIndex(uuid2, INDEX_NAME_FOR_QUERY, BasicPersonSearchable.F_NAME, BasicPersonSearchable.NAME_VALUE);
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
		elasticSearchRepository.getBulkIngester().flush();

		openIndex(FULL_INDEX_NAME_FOR_QUERY);

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, false);
	}

	@Test
	public void indexAsynchronousErrorNoRetryTest() {
		String uuid = UUID.randomUUID().toString();
		BasicPersonSearchable dummySearchable = indexBasicSearchable(uuid);

		elasticSearchRepository.index(dummySearchable, new IndexParameter());

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, BasicPersonSearchable.F_NAME, BasicPersonSearchable.NAME_VALUE);
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
			Assert.assertEquals(ElasticsearchException.class, e.getClass());
		} finally {
			openIndex(FULL_INDEX_NAME_FOR_QUERY);
		}

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, false);
	}

	@Test
	public void indexSynchronousErrorNoRetryTest() {
		String uuid = UUID.randomUUID().toString();
		BasicPersonSearchable dummySearchable = indexBasicSearchable(uuid);

		try {
			elasticSearchRepository.index(dummySearchable, new IndexParameter(Synchronization.SYNCHRONOUS));
		} catch (Exception e) {
			Assert.assertFalse(elasticSearchRepository.exceptionIsToBeRetried(e));
		}

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, BasicPersonSearchable.F_NAME, BasicPersonSearchable.NAME_VALUE);
	}

	@Test
	public void fetchSearchableFromIndexTest() {
		String uuid = UUID.randomUUID().toString();
		BasicPersonSearchable dummySearchable = indexBasicSearchable(uuid);

		String indexName = dummySearchable.getIndexName().concat("-*");
		SearchQuery query = new SearchQuery();
		query.setFields(List.of(BasicPersonSearchable.F_NAME));

		LOGGER.info("---check--- {}, {}, {}", elasticSearchRepository, indexName, query);
		SearchResult result = elasticSearchRepository.fetch(indexName, query);
		Assert.assertTrue("At least one record expected, SearchResult:" + result, result.getHits().size() >= 1);
	}

	@Test
	public void getAllIndicesTest() {
		indexBasicSearchable(UUID.randomUUID().toString());
		Set<String> allIndices = elasticSearchRepository.getAllIndices();
		Assert.assertEquals(1, allIndices.size());
		Assert.assertEquals("basicindex-no-date-v1", allIndices.toArray()[0]);
	}

	@Test
	public void getIndicesOfSearchableTest() {
		indexBasicSearchable(UUID.randomUUID().toString());
		List<String> indices = elasticSearchRepository.getIndicesOfSearchable(BasicPersonSearchable.class);
		Assert.assertEquals(1, indices.size());
	}

	/*
	Test Helper Methods
	 */
	private BasicPersonSearchable indexBasicSearchable(String uuid) {
		BasicPersonSearchable dummySearchable = getBasicSearchable(uuid);
		elasticSearchRepository.index(dummySearchable);

		validateDocumentInIndex(uuid, INDEX_NAME_FOR_QUERY, true);

		return dummySearchable;
	}

	private BasicPersonSearchable getBasicSearchable(String uuid) {
		BasicPersonSearchable dummySearchable = new BasicPersonSearchable();
		dummySearchable.setBusinessId(uuid);
		return dummySearchable;
	}
}

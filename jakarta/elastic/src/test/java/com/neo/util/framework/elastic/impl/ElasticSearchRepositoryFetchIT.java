package com.neo.util.framework.elastic.impl;

import com.neo.util.common.impl.enumeration.Synchronization;
import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.persistence.aggregation.*;
import com.neo.util.framework.api.persistence.criteria.*;
import com.neo.util.framework.api.persistence.search.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class ElasticSearchRepositoryFetchIT extends AbstractElasticIntegrationTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRepositoryFetchIT.class);

	protected static final String INDEX_NAME_FOR_QUERY = BasicPersonSearchable.INDEX_NAME + "-*";
	protected static final String FULL_INDEX_NAME_FOR_QUERY = BasicPersonSearchable.INDEX_NAME + "-no-date-v1";

	protected static final int MAX_NUMER_OF_SEARCHABLES = 4;

	protected BasicPersonSearchable personOne;
	protected BasicPersonSearchable personTwo;
	protected BasicPersonSearchable personThree;
	protected BasicPersonSearchable personFour;

	@Before
	public void initSearchable() throws Exception{
		personOne = createSearchable("Heaven Schneider",10,40.0,null);
		Thread.sleep(1);
		personTwo = createSearchable("Catherine Leon",20,45.0, false);
		Thread.sleep(1);
		personThree = createSearchable("Davian Chang",30,50.0,false);
		Thread.sleep(1);
		personFour = createSearchable("Gabriel Ryan",40,55.0,true);

		elasticSearchRepository.index(List.of(personOne, personTwo, personThree, personFour), new IndexParameter(Synchronization.SYNCHRONOUS));
	}

	@Test
	public void fetchSearchableFromIndexTest() {
		SearchQuery query = new SearchQuery();
		query.setFields(List.of(Searchable.BUSINESS_ID));

		LOGGER.info("---check--- {}, {}, {}", elasticSearchRepository, INDEX_NAME_FOR_QUERY, query);
		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();
			SearchResult result = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, query);
			return result.getHits().size() == 4;
		});
	}


	@Test
	public void longRangeSearchTest() {
		//Arrange
		List<String> desiredField = List.of(Searchable.BUSINESS_ID);

		SearchQuery fromQuery = new SearchQuery(desiredField, MAX_NUMER_OF_SEARCHABLES, List.of(
				new LongRangeSearchCriteria(BasicPersonSearchable.F_AGE, 15L,null)
		));
		SearchQuery toQuery = new SearchQuery(desiredField, MAX_NUMER_OF_SEARCHABLES, List.of(
				new LongRangeSearchCriteria(BasicPersonSearchable.F_AGE, null,35L)
		));
		SearchQuery betweenQuery = new SearchQuery(desiredField, MAX_NUMER_OF_SEARCHABLES, List.of(
				new LongRangeSearchCriteria(BasicPersonSearchable.F_AGE, 15L,35L)
		));
		//Act
		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();
			SearchResult fromResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, fromQuery);
			SearchResult toResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, toQuery);
			SearchResult betweenResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, betweenQuery);
			//Assert

			Assertions.assertEquals(3, fromResult.getHitSize());
			Assertions.assertEquals(personTwo.getBusinessId(), fromResult.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personThree.getBusinessId(), fromResult.getHits().get(1).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personFour.getBusinessId(), fromResult.getHits().get(2).get(Searchable.BUSINESS_ID).asText());

			Assertions.assertEquals(3, toResult.getHitSize());
			Assertions.assertEquals(personOne.getBusinessId(), toResult.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personTwo.getBusinessId(), toResult.getHits().get(1).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personThree.getBusinessId(), toResult.getHits().get(2).get(Searchable.BUSINESS_ID).asText());

			Assertions.assertEquals(2, betweenResult.getHitSize());
			Assertions.assertEquals(personTwo.getBusinessId(), betweenResult.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personThree.getBusinessId(), betweenResult.getHits().get(1).get(Searchable.BUSINESS_ID).asText());
			return true;
		});
	}

	@Test
	public void doubleRangeSearchTest() {
		//Arrange
		List<String> desiredField = List.of(BasicPersonSearchable.F_WEIGHT);

		SearchQuery fromQuery = new SearchQuery(desiredField, MAX_NUMER_OF_SEARCHABLES, List.of(
				new DoubleRangeSearchCriteria(BasicPersonSearchable.F_WEIGHT, 42.5,null)
		));
		SearchQuery toQuery = new SearchQuery(desiredField, MAX_NUMER_OF_SEARCHABLES, List.of(
				new DoubleRangeSearchCriteria(BasicPersonSearchable.F_WEIGHT, null,42.5)
		));
		SearchQuery betweenQuery = new SearchQuery(desiredField, MAX_NUMER_OF_SEARCHABLES, List.of(
				new DoubleRangeSearchCriteria(BasicPersonSearchable.F_WEIGHT, 42.5,52.5)
		));
		//Act
		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();
			SearchResult fromResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, fromQuery);
			SearchResult toResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, toQuery);
			SearchResult betweenResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, betweenQuery);
			//Assert

			Assertions.assertEquals(3, fromResult.getHitSize());
			Assertions.assertEquals(personTwo.getWeight(),
					fromResult.getHits().get(0).get(BasicPersonSearchable.F_WEIGHT).asDouble());
			Assertions.assertEquals(personThree.getWeight(),
					fromResult.getHits().get(1).get(BasicPersonSearchable.F_WEIGHT).asDouble());
			Assertions.assertEquals(personFour.getWeight(),
					fromResult.getHits().get(2).get(BasicPersonSearchable.F_WEIGHT).asDouble());

			Assertions.assertEquals(1, toResult.getHitSize());
			Assertions.assertEquals(personOne.getWeight(),
					toResult.getHits().get(0).get(BasicPersonSearchable.F_WEIGHT).asDouble());

			Assertions.assertEquals(2, betweenResult.getHitSize());
			Assertions.assertEquals(personTwo.getWeight(),
					betweenResult.getHits().get(0).get(BasicPersonSearchable.F_WEIGHT).asDouble());
			Assertions.assertEquals(personThree.getWeight(),
					betweenResult.getHits().get(1).get(BasicPersonSearchable.F_WEIGHT).asDouble());
			return true;
		});
	}

	@Test
	public void explicitBooleanSearchSearchTest() {
		//Arrange
		SearchQuery searchQuery = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new ExplicitSearchCriteria(BasicPersonSearchable.F_HAS_TWO_ARMS, true)
		));
		//Act

		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();
			SearchResult searchResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, searchQuery);
			//Assert

			Assertions.assertEquals(1, searchResult.getHitSize());
			Assertions.assertEquals(personFour.getBusinessId(), searchResult.getHits().get(0).get(Searchable.BUSINESS_ID).textValue());
			return true;
		});
	}

	@Test
	public void explicitIntegerSearchSearchTest() {
		//Arrange
		SearchQuery searchQuery = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new ExplicitSearchCriteria(BasicPersonSearchable.F_AGE, 20)
		));
		//Act

		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();
			SearchResult searchResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, searchQuery);
			//Assert

			Assertions.assertEquals(1, searchResult.getHitSize());
			Assertions.assertEquals(personTwo.getBusinessId(), searchResult.getHits().get(0).get(Searchable.BUSINESS_ID).textValue());
			return true;
		});
	}

	@Test
	public void explicitWildcardSearchTest() {
		//Arrange

		SearchQuery starQuery = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new ExplicitSearchCriteria(BasicPersonSearchable.F_NAME + ".keyword", "*Leon*", true)
		));

		SearchQuery questionQuery = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new ExplicitSearchCriteria(BasicPersonSearchable.F_NAME + ".keyword", "Heaven Schne?der", true)
		));
		//Act
		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();
			SearchResult starResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, starQuery);
			SearchResult questionResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, questionQuery);

			Assertions.assertEquals(1, starResult.getHitSize());
			Assertions.assertEquals(personTwo.getBusinessId(), starResult.getHits().get(0).get(Searchable.BUSINESS_ID).textValue());

			Assertions.assertEquals(1, questionResult.getHitSize());
			Assertions.assertEquals(personOne.getBusinessId() ,questionResult.getHits().get(0).get(Searchable.BUSINESS_ID).textValue());
			return true;
		});
	}

	@Test
	public void explicitStringSearchSearchTest() {
		//Arrange

		SearchQuery query = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new ExplicitSearchCriteria(BasicPersonSearchable.F_NAME + ".keyword", "Catherine Leon")
		));
		//Act
		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();
			SearchResult result = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, query);

			//Assert
			Assertions.assertEquals(1, result.getHitSize());
			Assertions.assertEquals(personTwo.getBusinessId(), result.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			return true;

		});
	}

	@Test
	public void existingSearchTest() {
		//Arrange
		SearchQuery query = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new ExistingFieldSearchCriteria(BasicPersonSearchable.F_HAS_TWO_ARMS)
		));

		//Act
		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();
			SearchResult result = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, query);

			//Assert
			Assertions.assertEquals(3, result.getHitSize());
			Assertions.assertEquals(personTwo.getBusinessId(), result.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personThree.getBusinessId(), result.getHits().get(1).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personFour.getBusinessId(), result.getHits().get(2).get(Searchable.BUSINESS_ID).asText());
			return true;
		});
	}


	@Test
	public void dateRangeSearchTest() {
		//Arrange
		SearchQuery fromQuery = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new DateSearchCriteria(BasicPersonSearchable.F_TIMESTAMP, personTwo.getTimestamp(), null)));
		SearchQuery toQuery = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new DateSearchCriteria(BasicPersonSearchable.F_TIMESTAMP, null, personTwo.getTimestamp())));
		SearchQuery betweenQuery = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new DateSearchCriteria(BasicPersonSearchable.F_TIMESTAMP, personTwo.getTimestamp(), personThree.getTimestamp())));

		//Act
		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();

			SearchResult fromResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, fromQuery);
			SearchResult toResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, toQuery);
			SearchResult betweenResult = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, betweenQuery);
			//Assert

			Assertions.assertEquals(3,fromResult.getHitSize());
			Assertions.assertEquals(personTwo.getBusinessId(), fromResult.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personThree.getBusinessId(), fromResult.getHits().get(1).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personFour.getBusinessId(), fromResult.getHits().get(2).get(Searchable.BUSINESS_ID).asText());

			Assertions.assertEquals(2, toResult.getHitSize());
			Assertions.assertEquals(personOne.getBusinessId(), toResult.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personTwo.getBusinessId(), toResult.getHits().get(1).get(Searchable.BUSINESS_ID).asText());

			Assertions.assertEquals(2,betweenResult.getHitSize());
			Assertions.assertEquals(personTwo.getBusinessId(), betweenResult.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personThree.getBusinessId(), betweenResult.getHits().get(1).get(Searchable.BUSINESS_ID).asText());
			return true;
		});
	}

	@Test
	public void containsSearchTest() {
		//Arrange
		SearchQuery query = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new ContainsSearchCriteria(BasicPersonSearchable.F_AGE, 0, 10,40)));
		//Act
		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();

			SearchResult result = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, query);

			//Assert
			Assertions.assertEquals(2, result.getHitSize());
			Assertions.assertEquals(personOne.getBusinessId(), result.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personFour.getBusinessId(), result.getHits().get(1).get(Searchable.BUSINESS_ID).asText());
			return true;
		});
	}

	@Test
	public void combinedSearchAndTest() {
		//Arrange
		SearchQuery query = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new CombinedSearchCriteria(
						new ContainsSearchCriteria(BasicPersonSearchable.F_AGE, 0, 10,40),
						new ExplicitSearchCriteria(BasicPersonSearchable.F_NAME, "*ne*", true))));
		//Act
		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();

			SearchResult result = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, query);

			//Assert
			Assertions.assertEquals(1, result.getHitSize());
			Assertions.assertEquals(personOne.getBusinessId(), result.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			return true;
		});
	}

	@Test
	public void combinedSearchOrTest() {
		//Arrange
		SearchQuery query = new SearchQuery(List.of(Searchable.BUSINESS_ID), MAX_NUMER_OF_SEARCHABLES, List.of(
				new CombinedSearchCriteria(CombinedSearchCriteria.Association.OR,
						new ContainsSearchCriteria(BasicPersonSearchable.F_AGE, 0, 10,40),
						new ExplicitSearchCriteria(BasicPersonSearchable.F_NAME, "*ne*", true))));
		//Act
		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();

			SearchResult result = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, query);

			//Assert
			Assertions.assertEquals(3, result.getHitSize());
			Assertions.assertEquals(personOne.getBusinessId(), result.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personTwo.getBusinessId(), result.getHits().get(1).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personFour.getBusinessId(), result.getHits().get(2).get(Searchable.BUSINESS_ID).asText());
			return true;
		});

	}

	@Test
	public void orderTest() {
		//Arrange
		int maxResult = 2;

		SearchQuery query = new SearchQuery(List.of(Searchable.BUSINESS_ID),0 ,maxResult,
				null ,List.of(), Map.of(BasicPersonSearchable.F_WEIGHT, false), List.of(), false);
		//Act
		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();

			SearchResult result = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, query);

			//Assert
			Assertions.assertEquals(maxResult, result.getHits().size());
			Assertions.assertEquals(4, result.getHitSize());
			Assertions.assertEquals(personFour.getBusinessId(), result.getHits().get(0).get(Searchable.BUSINESS_ID).asText());
			Assertions.assertEquals(personThree.getBusinessId(), result.getHits().get(1).get(Searchable.BUSINESS_ID).asText());
			return true;
		});
	}

	@Test
	public void simpleFieldAggregationTest() {
		Function<AggregationResult, Double> parse = result -> (Double) ((SimpleAggregationResult) result).getValue();

		SearchAggregation count = new SimpleFieldAggregation("COUNT", BasicPersonSearchable.F_AGE,
				SimpleFieldAggregation.Type.COUNT);
		SearchAggregation sum = new SimpleFieldAggregation("SUM", BasicPersonSearchable.F_AGE,
				SimpleFieldAggregation.Type.SUM);
		SearchAggregation avg = new SimpleFieldAggregation("AVG", BasicPersonSearchable.F_AGE,
				SimpleFieldAggregation.Type.AVG);
		SearchAggregation min = new SimpleFieldAggregation("MIN", BasicPersonSearchable.F_AGE,
				SimpleFieldAggregation.Type.MIN);
		SearchAggregation max = new SimpleFieldAggregation("MAX", BasicPersonSearchable.F_AGE,
				SimpleFieldAggregation.Type.MAX);

		SearchQuery query = new SearchQuery(List.of(Searchable.BUSINESS_ID),0 ,0,
				null ,List.of(), Map.of(), List.of(count, sum, avg, min, max), false);

		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();

			SearchResult result = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, query);

			Assertions.assertEquals(4.0, parse.apply(result.getAggregations().get("COUNT")));
			Assertions.assertEquals(100.0, parse.apply(result.getAggregations().get("SUM")));
			Assertions.assertEquals(25.0, parse.apply(result.getAggregations().get("AVG")));
			Assertions.assertEquals(10.0, parse.apply(result.getAggregations().get("MIN")));
			Assertions.assertEquals(40.0, parse.apply(result.getAggregations().get("MAX")));
			//Assert
			return true;
		});
	}

	@Test
	public void criteriaAggregationTest() {
		CriteriaAggregation criteriaAggregation = new CriteriaAggregation(
				"criteriaAggregation",
				Map.of(
						"0",
						new DoubleRangeSearchCriteria(BasicPersonSearchable.F_AGE, 10.0, 20.0),
						"1",
						new DoubleRangeSearchCriteria(BasicPersonSearchable.F_AGE, 30.0, 40.0)
				),
				new SimpleFieldAggregation("SUM", BasicPersonSearchable.F_AGE,
						SimpleFieldAggregation.Type.SUM)
		);

		SearchQuery query = new SearchQuery(List.of(Searchable.BUSINESS_ID),0 ,0,
				null ,List.of(), Map.of(), List.of(criteriaAggregation), false);

		IntegrationTestUtil.sleepUntil(TIME_TO_SLEEP_IN_MILLISECOND, SLEEP_RETRY_COUNT, () -> {
			flushAndRefresh();

			SearchResult result = elasticSearchRepository.fetch(INDEX_NAME_FOR_QUERY, query);

			Assertions.assertEquals(1, result.getAggregations().size());

			CriteriaAggregationResult aggregationResult = (CriteriaAggregationResult) result.getAggregations().get("criteriaAggregation");

			Assertions.assertEquals(30.0, aggregationResult.getCriteriaResult().get("0"));
			Assertions.assertEquals(70.0, aggregationResult.getCriteriaResult().get("1"));
			//Assert
			return true;
		});
	}

	protected BasicPersonSearchable createSearchable(String name, int age, Double weight, Boolean hasTwoArms) {
		return new BasicPersonSearchable(UUID.randomUUID().toString(), name, age, weight, hasTwoArms);
	}

}
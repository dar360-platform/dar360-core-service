package com.lending.dar360UserService.user.repository;

import com.lending.dar360UserService.user.dto.SearchUserForm;
import com.lending.dar360UserService.user.model.User;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class UserSpecificationTest {

    @InjectMocks
    private UserSpecification userSpecification;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(this.userSpecification, "searchUserForm", new SearchUserForm());
    }

    @Test
    public void toPredicate() {
        Root<User> root = createRootUser();
        CriteriaQuery<?> query = null;
        CriteriaBuilder criteriaBuilder = createCriteriaBuilder();
        Assert.assertNull(this.userSpecification.toPredicate(root, query, criteriaBuilder));
    }

    @Test
    public void toPredicate_fullParams() {
        Root<User> root = createRootUser();
        CriteriaQuery<?> query = null;
        CriteriaBuilder criteriaBuilder = createCriteriaBuilder();
        ReflectionTestUtils.setField(this.userSpecification, "searchUserForm", createSearchUserForm());
        Assert.assertNull(this.userSpecification.toPredicate(root, query, criteriaBuilder));
    }

    private SearchUserForm createSearchUserForm() {
        SearchUserForm searchUserForm = new SearchUserForm();
        searchUserForm.setIds(Set.of(UUID.randomUUID()));
        searchUserForm.setKeyword("keyword");
        searchUserForm.setFromDate("2020-02-02");
        searchUserForm.setToDate("2020-02-02");
        searchUserForm.setStatus(1);
        return searchUserForm;
    }

    private CriteriaBuilder createCriteriaBuilder() {
        return new CriteriaBuilder() {
            @Override
            public CriteriaQuery<Object> createQuery() {
                return null;
            }

            @Override
            public <T> CriteriaQuery<T> createQuery(Class<T> aClass) {
                return null;
            }

            @Override
            public CriteriaQuery<Tuple> createTupleQuery() {
                return null;
            }

            @Override
            public <T> CriteriaUpdate<T> createCriteriaUpdate(Class<T> aClass) {
                return null;
            }

            @Override
            public <T> CriteriaDelete<T> createCriteriaDelete(Class<T> aClass) {
                return null;
            }

            @Override
            public <Y> CompoundSelection<Y> construct(Class<Y> aClass, Selection<?>... selections) {
                return null;
            }

            @Override
            public CompoundSelection<Tuple> tuple(Selection<?>... selections) {
                return null;
            }

            @Override
            public CompoundSelection<Object[]> array(Selection<?>... selections) {
                return null;
            }

            @Override
            public Order asc(Expression<?> expression) {
                return null;
            }

            @Override
            public Order desc(Expression<?> expression) {
                return null;
            }

            @Override
            public <N extends Number> Expression<Double> avg(Expression<N> expression) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> sum(Expression<N> expression) {
                return null;
            }

            @Override
            public Expression<Long> sumAsLong(Expression<Integer> expression) {
                return null;
            }

            @Override
            public Expression<Double> sumAsDouble(Expression<Float> expression) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> max(Expression<N> expression) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> min(Expression<N> expression) {
                return null;
            }

            @Override
            public <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> expression) {
                return null;
            }

            @Override
            public <X extends Comparable<? super X>> Expression<X> least(Expression<X> expression) {
                return null;
            }

            @Override
            public Expression<Long> count(Expression<?> expression) {
                return null;
            }

            @Override
            public Expression<Long> countDistinct(Expression<?> expression) {
                return null;
            }

            @Override
            public Predicate exists(Subquery<?> subquery) {
                return null;
            }

            @Override
            public <Y> Expression<Y> all(Subquery<Y> subquery) {
                return null;
            }

            @Override
            public <Y> Expression<Y> some(Subquery<Y> subquery) {
                return null;
            }

            @Override
            public <Y> Expression<Y> any(Subquery<Y> subquery) {
                return null;
            }

            @Override
            public Predicate and(Expression<Boolean> expression, Expression<Boolean> expression1) {
                return null;
            }

            @Override
            public Predicate and(Predicate... predicates) {
                return null;
            }

            @Override
            public Predicate or(Expression<Boolean> expression, Expression<Boolean> expression1) {
                return null;
            }

            @Override
            public Predicate or(Predicate... predicates) {
                return null;
            }

            @Override
            public Predicate not(Expression<Boolean> expression) {
                return null;
            }

            @Override
            public Predicate conjunction() {
                return null;
            }

            @Override
            public Predicate disjunction() {
                return null;
            }

            @Override
            public Predicate isTrue(Expression<Boolean> expression) {
                return null;
            }

            @Override
            public Predicate isFalse(Expression<Boolean> expression) {
                return null;
            }

            @Override
            public Predicate isNull(Expression<?> expression) {
                return null;
            }

            @Override
            public Predicate isNotNull(Expression<?> expression) {
                return null;
            }

            @Override
            public Predicate equal(Expression<?> expression, Expression<?> expression1) {
                return null;
            }

            @Override
            public Predicate equal(Expression<?> expression, Object o) {
                return null;
            }

            @Override
            public Predicate notEqual(Expression<?> expression, Expression<?> expression1) {
                return null;
            }

            @Override
            public Predicate notEqual(Expression<?> expression, Object o) {
                return null;
            }

            @Override
            public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> expression, Expression<? extends Y> expression1) {
                return null;
            }

            @Override
            public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> expression, Y y) {
                return null;
            }

            @Override
            public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> expression, Expression<? extends Y> expression1) {
                return null;
            }

            @Override
            public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(Expression<? extends Y> expression, Y y) {
                return null;
            }

            @Override
            public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> expression, Expression<? extends Y> expression1) {
                return null;
            }

            @Override
            public <Y extends Comparable<? super Y>> Predicate lessThan(Expression<? extends Y> expression, Y y) {
                return null;
            }

            @Override
            public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> expression, Expression<? extends Y> expression1) {
                return null;
            }

            @Override
            public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(Expression<? extends Y> expression, Y y) {
                return null;
            }

            @Override
            public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> expression, Expression<? extends Y> expression1, Expression<? extends Y> expression2) {
                return null;
            }

            @Override
            public <Y extends Comparable<? super Y>> Predicate between(Expression<? extends Y> expression, Y y, Y y1) {
                return null;
            }

            @Override
            public Predicate gt(Expression<? extends Number> expression, Expression<? extends Number> expression1) {
                return null;
            }

            @Override
            public Predicate gt(Expression<? extends Number> expression, Number number) {
                return null;
            }

            @Override
            public Predicate ge(Expression<? extends Number> expression, Expression<? extends Number> expression1) {
                return null;
            }

            @Override
            public Predicate ge(Expression<? extends Number> expression, Number number) {
                return null;
            }

            @Override
            public Predicate lt(Expression<? extends Number> expression, Expression<? extends Number> expression1) {
                return null;
            }

            @Override
            public Predicate lt(Expression<? extends Number> expression, Number number) {
                return null;
            }

            @Override
            public Predicate le(Expression<? extends Number> expression, Expression<? extends Number> expression1) {
                return null;
            }

            @Override
            public Predicate le(Expression<? extends Number> expression, Number number) {
                return null;
            }

            @Override
            public Expression<Integer> sign(Expression<? extends Number> expression) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> neg(Expression<N> expression) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> abs(Expression<N> expression) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> ceiling(Expression<N> expression) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> floor(Expression<N> expression) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> sum(Expression<? extends N> expression, Expression<? extends N> expression1) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> sum(Expression<? extends N> expression, N n) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> sum(N n, Expression<? extends N> expression) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> prod(Expression<? extends N> expression, Expression<? extends N> expression1) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> prod(Expression<? extends N> expression, N n) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> prod(N n, Expression<? extends N> expression) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> diff(Expression<? extends N> expression, Expression<? extends N> expression1) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> diff(Expression<? extends N> expression, N n) {
                return null;
            }

            @Override
            public <N extends Number> Expression<N> diff(N n, Expression<? extends N> expression) {
                return null;
            }

            @Override
            public Expression<Number> quot(Expression<? extends Number> expression, Expression<? extends Number> expression1) {
                return null;
            }

            @Override
            public Expression<Number> quot(Expression<? extends Number> expression, Number number) {
                return null;
            }

            @Override
            public Expression<Number> quot(Number number, Expression<? extends Number> expression) {
                return null;
            }

            @Override
            public Expression<Integer> mod(Expression<Integer> expression, Expression<Integer> expression1) {
                return null;
            }

            @Override
            public Expression<Integer> mod(Expression<Integer> expression, Integer integer) {
                return null;
            }

            @Override
            public Expression<Integer> mod(Integer integer, Expression<Integer> expression) {
                return null;
            }

            @Override
            public Expression<Double> sqrt(Expression<? extends Number> expression) {
                return null;
            }

            @Override
            public Expression<Double> exp(Expression<? extends Number> expression) {
                return null;
            }

            @Override
            public Expression<Double> ln(Expression<? extends Number> expression) {
                return null;
            }

            @Override
            public Expression<Double> power(Expression<? extends Number> expression, Expression<? extends Number> expression1) {
                return null;
            }

            @Override
            public Expression<Double> power(Expression<? extends Number> expression, Number number) {
                return null;
            }

            @Override
            public <T extends Number> Expression<T> round(Expression<T> expression, Integer integer) {
                return null;
            }

            @Override
            public Expression<Long> toLong(Expression<? extends Number> expression) {
                return null;
            }

            @Override
            public Expression<Integer> toInteger(Expression<? extends Number> expression) {
                return null;
            }

            @Override
            public Expression<Float> toFloat(Expression<? extends Number> expression) {
                return null;
            }

            @Override
            public Expression<Double> toDouble(Expression<? extends Number> expression) {
                return null;
            }

            @Override
            public Expression<BigDecimal> toBigDecimal(Expression<? extends Number> expression) {
                return null;
            }

            @Override
            public Expression<BigInteger> toBigInteger(Expression<? extends Number> expression) {
                return null;
            }

            @Override
            public Expression<String> toString(Expression<Character> expression) {
                return null;
            }

            @Override
            public <T> Expression<T> literal(T t) {
                return null;
            }

            @Override
            public <T> Expression<T> nullLiteral(Class<T> aClass) {
                return null;
            }

            @Override
            public <T> ParameterExpression<T> parameter(Class<T> aClass) {
                return null;
            }

            @Override
            public <T> ParameterExpression<T> parameter(Class<T> aClass, String s) {
                return null;
            }

            @Override
            public <C extends Collection<?>> Predicate isEmpty(Expression<C> expression) {
                return null;
            }

            @Override
            public <C extends Collection<?>> Predicate isNotEmpty(Expression<C> expression) {
                return null;
            }

            @Override
            public <C extends Collection<?>> Expression<Integer> size(Expression<C> expression) {
                return null;
            }

            @Override
            public <C extends Collection<?>> Expression<Integer> size(C objects) {
                return null;
            }

            @Override
            public <E, C extends Collection<E>> Predicate isMember(Expression<E> expression, Expression<C> expression1) {
                return null;
            }

            @Override
            public <E, C extends Collection<E>> Predicate isMember(E e, Expression<C> expression) {
                return null;
            }

            @Override
            public <E, C extends Collection<E>> Predicate isNotMember(Expression<E> expression, Expression<C> expression1) {
                return null;
            }

            @Override
            public <E, C extends Collection<E>> Predicate isNotMember(E e, Expression<C> expression) {
                return null;
            }

            @Override
            public <V, M extends Map<?, V>> Expression<Collection<V>> values(M m) {
                return null;
            }

            @Override
            public <K, M extends Map<K, ?>> Expression<Set<K>> keys(M m) {
                return null;
            }

            @Override
            public Predicate like(Expression<String> expression, Expression<String> expression1) {
                return null;
            }

            @Override
            public Predicate like(Expression<String> expression, String s) {
                return null;
            }

            @Override
            public Predicate like(Expression<String> expression, Expression<String> expression1, Expression<Character> expression2) {
                return null;
            }

            @Override
            public Predicate like(Expression<String> expression, Expression<String> expression1, char c) {
                return null;
            }

            @Override
            public Predicate like(Expression<String> expression, String s, Expression<Character> expression1) {
                return null;
            }

            @Override
            public Predicate like(Expression<String> expression, String s, char c) {
                return null;
            }

            @Override
            public Predicate notLike(Expression<String> expression, Expression<String> expression1) {
                return null;
            }

            @Override
            public Predicate notLike(Expression<String> expression, String s) {
                return null;
            }

            @Override
            public Predicate notLike(Expression<String> expression, Expression<String> expression1, Expression<Character> expression2) {
                return null;
            }

            @Override
            public Predicate notLike(Expression<String> expression, Expression<String> expression1, char c) {
                return null;
            }

            @Override
            public Predicate notLike(Expression<String> expression, String s, Expression<Character> expression1) {
                return null;
            }

            @Override
            public Predicate notLike(Expression<String> expression, String s, char c) {
                return null;
            }

            @Override
            public Expression<String> concat(Expression<String> expression, Expression<String> expression1) {
                return null;
            }

            @Override
            public Expression<String> concat(Expression<String> expression, String s) {
                return null;
            }

            @Override
            public Expression<String> concat(String s, Expression<String> expression) {
                return null;
            }

            @Override
            public Expression<String> substring(Expression<String> expression, Expression<Integer> expression1) {
                return null;
            }

            @Override
            public Expression<String> substring(Expression<String> expression, int i) {
                return null;
            }

            @Override
            public Expression<String> substring(Expression<String> expression, Expression<Integer> expression1, Expression<Integer> expression2) {
                return null;
            }

            @Override
            public Expression<String> substring(Expression<String> expression, int i, int i1) {
                return null;
            }

            @Override
            public Expression<String> trim(Expression<String> expression) {
                return null;
            }

            @Override
            public Expression<String> trim(Trimspec trimspec, Expression<String> expression) {
                return null;
            }

            @Override
            public Expression<String> trim(Expression<Character> expression, Expression<String> expression1) {
                return null;
            }

            @Override
            public Expression<String> trim(Trimspec trimspec, Expression<Character> expression, Expression<String> expression1) {
                return null;
            }

            @Override
            public Expression<String> trim(char c, Expression<String> expression) {
                return null;
            }

            @Override
            public Expression<String> trim(Trimspec trimspec, char c, Expression<String> expression) {
                return null;
            }

            @Override
            public Expression<String> lower(Expression<String> expression) {
                return null;
            }

            @Override
            public Expression<String> upper(Expression<String> expression) {
                return null;
            }

            @Override
            public Expression<Integer> length(Expression<String> expression) {
                return null;
            }

            @Override
            public Expression<Integer> locate(Expression<String> expression, Expression<String> expression1) {
                return null;
            }

            @Override
            public Expression<Integer> locate(Expression<String> expression, String s) {
                return null;
            }

            @Override
            public Expression<Integer> locate(Expression<String> expression, Expression<String> expression1, Expression<Integer> expression2) {
                return null;
            }

            @Override
            public Expression<Integer> locate(Expression<String> expression, String s, int i) {
                return null;
            }

            @Override
            public Expression<Date> currentDate() {
                return null;
            }

            @Override
            public Expression<Timestamp> currentTimestamp() {
                return null;
            }

            @Override
            public Expression<Time> currentTime() {
                return null;
            }

            @Override
            public Expression<LocalDate> localDate() {
                return null;
            }

            @Override
            public Expression<LocalDateTime> localDateTime() {
                return null;
            }

            @Override
            public Expression<LocalTime> localTime() {
                return null;
            }

            @Override
            public <T> In<T> in(Expression<? extends T> expression) {
                return new In<T>() {
                    @Override
                    public Expression<T> getExpression() {
                        return null;
                    }

                    @Override
                    public In<T> value(T t) {
                        return null;
                    }

                    @Override
                    public In<T> value(Expression<? extends T> expression) {
                        return null;
                    }

                    @Override
                    public BooleanOperator getOperator() {
                        return null;
                    }

                    @Override
                    public boolean isNegated() {
                        return false;
                    }

                    @Override
                    public List<Expression<Boolean>> getExpressions() {
                        return List.of();
                    }

                    @Override
                    public Predicate not() {
                        return null;
                    }

                    @Override
                    public Predicate isNull() {
                        return null;
                    }

                    @Override
                    public Predicate isNotNull() {
                        return null;
                    }

                    @Override
                    public Predicate in(Object... objects) {
                        return null;
                    }

                    @Override
                    public Predicate in(Expression<?>... expressions) {
                        return null;
                    }

                    @Override
                    public Predicate in(Collection<?> collection) {
                        return null;
                    }

                    @Override
                    public Predicate in(Expression<Collection<?>> expression) {
                        return null;
                    }

                    @Override
                    public <X> Expression<X> as(Class<X> aClass) {
                        return null;
                    }

                    @Override
                    public Selection<Boolean> alias(String s) {
                        return null;
                    }

                    @Override
                    public boolean isCompoundSelection() {
                        return false;
                    }

                    @Override
                    public List<Selection<?>> getCompoundSelectionItems() {
                        return List.of();
                    }

                    @Override
                    public Class<? extends Boolean> getJavaType() {
                        return null;
                    }

                    @Override
                    public String getAlias() {
                        return "";
                    }
                };
            }

            @Override
            public <Y> Expression<Y> coalesce(Expression<? extends Y> expression, Expression<? extends Y> expression1) {
                return null;
            }

            @Override
            public <Y> Expression<Y> coalesce(Expression<? extends Y> expression, Y y) {
                return null;
            }

            @Override
            public <Y> Expression<Y> nullif(Expression<Y> expression, Expression<?> expression1) {
                return null;
            }

            @Override
            public <Y> Expression<Y> nullif(Expression<Y> expression, Y y) {
                return null;
            }

            @Override
            public <T> Coalesce<T> coalesce() {
                return null;
            }

            @Override
            public <C, R> SimpleCase<C, R> selectCase(Expression<? extends C> expression) {
                return null;
            }

            @Override
            public <R> Case<R> selectCase() {
                return null;
            }

            @Override
            public <T> Expression<T> function(String s, Class<T> aClass, Expression<?>... expressions) {
                return null;
            }

            @Override
            public <X, T, V extends T> Join<X, V> treat(Join<X, T> join, Class<V> aClass) {
                return null;
            }

            @Override
            public <X, T, E extends T> CollectionJoin<X, E> treat(CollectionJoin<X, T> collectionJoin, Class<E> aClass) {
                return null;
            }

            @Override
            public <X, T, E extends T> SetJoin<X, E> treat(SetJoin<X, T> setJoin, Class<E> aClass) {
                return null;
            }

            @Override
            public <X, T, E extends T> ListJoin<X, E> treat(ListJoin<X, T> listJoin, Class<E> aClass) {
                return null;
            }

            @Override
            public <X, K, T, V extends T> MapJoin<X, K, V> treat(MapJoin<X, K, T> mapJoin, Class<V> aClass) {
                return null;
            }

            @Override
            public <X, T extends X> Path<T> treat(Path<X> path, Class<T> aClass) {
                return null;
            }

            @Override
            public <X, T extends X> Root<T> treat(Root<X> root, Class<T> aClass) {
                return null;
            }
        };
    }

    private Root<User> createRootUser() {
        return new Root<User>() {
            @Override
            public EntityType<User> getModel() {
                return null;
            }

            @Override
            public Set<Join<User, ?>> getJoins() {
                return Set.of();
            }

            @Override
            public boolean isCorrelated() {
                return false;
            }

            @Override
            public From<User, User> getCorrelationParent() {
                return null;
            }

            @Override
            public <Y> Join<User, Y> join(SingularAttribute<? super User, Y> singularAttribute) {
                return null;
            }

            @Override
            public <Y> Join<User, Y> join(SingularAttribute<? super User, Y> singularAttribute, JoinType joinType) {
                return null;
            }

            @Override
            public <Y> CollectionJoin<User, Y> join(CollectionAttribute<? super User, Y> collectionAttribute) {
                return null;
            }

            @Override
            public <Y> SetJoin<User, Y> join(SetAttribute<? super User, Y> setAttribute) {
                return null;
            }

            @Override
            public <Y> ListJoin<User, Y> join(ListAttribute<? super User, Y> listAttribute) {
                return null;
            }

            @Override
            public <K, V> MapJoin<User, K, V> join(MapAttribute<? super User, K, V> mapAttribute) {
                return null;
            }

            @Override
            public <Y> CollectionJoin<User, Y> join(CollectionAttribute<? super User, Y> collectionAttribute, JoinType joinType) {
                return null;
            }

            @Override
            public <Y> SetJoin<User, Y> join(SetAttribute<? super User, Y> setAttribute, JoinType joinType) {
                return null;
            }

            @Override
            public <Y> ListJoin<User, Y> join(ListAttribute<? super User, Y> listAttribute, JoinType joinType) {
                return null;
            }

            @Override
            public <K, V> MapJoin<User, K, V> join(MapAttribute<? super User, K, V> mapAttribute, JoinType joinType) {
                return null;
            }

            @Override
            public <X, Y> Join<X, Y> join(String s) {
                return null;
            }

            @Override
            public <X, Y> CollectionJoin<X, Y> joinCollection(String s) {
                return null;
            }

            @Override
            public <X, Y> SetJoin<X, Y> joinSet(String s) {
                return null;
            }

            @Override
            public <X, Y> ListJoin<X, Y> joinList(String s) {
                return null;
            }

            @Override
            public <X, K, V> MapJoin<X, K, V> joinMap(String s) {
                return null;
            }

            @Override
            public <X, Y> Join<X, Y> join(String s, JoinType joinType) {
                return null;
            }

            @Override
            public <X, Y> CollectionJoin<X, Y> joinCollection(String s, JoinType joinType) {
                return null;
            }

            @Override
            public <X, Y> SetJoin<X, Y> joinSet(String s, JoinType joinType) {
                return null;
            }

            @Override
            public <X, Y> ListJoin<X, Y> joinList(String s, JoinType joinType) {
                return null;
            }

            @Override
            public <X, K, V> MapJoin<X, K, V> joinMap(String s, JoinType joinType) {
                return null;
            }

            @Override
            public Path<?> getParentPath() {
                return null;
            }

            @Override
            public <Y> Path<Y> get(SingularAttribute<? super User, Y> singularAttribute) {
                return null;
            }

            @Override
            public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<User, C, E> pluralAttribute) {
                return null;
            }

            @Override
            public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<User, K, V> mapAttribute) {
                return null;
            }

            @Override
            public Expression<Class<? extends User>> type() {
                return null;
            }

            @Override
            public <Y> Path<Y> get(String s) {
                return null;
            }

            @Override
            public Predicate isNull() {
                return null;
            }

            @Override
            public Predicate isNotNull() {
                return null;
            }

            @Override
            public Predicate in(Object... objects) {
                return null;
            }

            @Override
            public Predicate in(Expression<?>... expressions) {
                return null;
            }

            @Override
            public Predicate in(Collection<?> collection) {
                return null;
            }

            @Override
            public Predicate in(Expression<Collection<?>> expression) {
                return null;
            }

            @Override
            public <X> Expression<X> as(Class<X> aClass) {
                return null;
            }

            @Override
            public Set<Fetch<User, ?>> getFetches() {
                return Set.of();
            }

            @Override
            public <Y> Fetch<User, Y> fetch(SingularAttribute<? super User, Y> singularAttribute) {
                return null;
            }

            @Override
            public <Y> Fetch<User, Y> fetch(SingularAttribute<? super User, Y> singularAttribute, JoinType joinType) {
                return null;
            }

            @Override
            public <Y> Fetch<User, Y> fetch(PluralAttribute<? super User, ?, Y> pluralAttribute) {
                return null;
            }

            @Override
            public <Y> Fetch<User, Y> fetch(PluralAttribute<? super User, ?, Y> pluralAttribute, JoinType joinType) {
                return null;
            }

            @Override
            public <X, Y> Fetch<X, Y> fetch(String s) {
                return null;
            }

            @Override
            public <X, Y> Fetch<X, Y> fetch(String s, JoinType joinType) {
                return null;
            }

            @Override
            public Selection<User> alias(String s) {
                return null;
            }

            @Override
            public boolean isCompoundSelection() {
                return false;
            }

            @Override
            public List<Selection<?>> getCompoundSelectionItems() {
                return List.of();
            }

            @Override
            public Class<? extends User> getJavaType() {
                return null;
            }

            @Override
            public String getAlias() {
                return "";
            }
        };
    }
}
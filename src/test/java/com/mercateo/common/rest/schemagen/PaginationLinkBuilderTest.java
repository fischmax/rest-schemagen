package com.mercateo.common.rest.schemagen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import com.mercateo.common.rest.schemagen.link.relation.Rel;
import com.mercateo.common.rest.schemagen.link.relation.Relation;
import com.mercateo.common.rest.schemagen.link.relation.RelationContainer;

@SuppressWarnings("unused")
public class PaginationLinkBuilderTest {

    private static final int LIMIT = 5;

    private static final int LIMIT_2 = 7;

    private static final int TOTAL = LIMIT_2 * LIMIT_2;

    private static final int OFFSET = 17;

    private static final int OFFSET_FOR_LAST = 45;

    DummyBuilder dummyBuilder = new DummyBuilder();

    @SuppressWarnings("boxing")
    @Test
    public void test() {

        PaginationLinkBuilder paginationLinkBuilder = PaginationLinkBuilder
                .of(TOTAL, OFFSET, LIMIT);
        final List<Optional<Result>> links = new ArrayList<>();
        createLinks(paginationLinkBuilder, links);

        assertThat(filter(links, Rel.SELF).get().offset).isEqualTo(OFFSET);
        assertThat(filter(links, Rel.SELF).get().limit).isEqualTo(LIMIT);

        assertThat(filter(links, Rel.NEXT).get().offset).isEqualTo(OFFSET + LIMIT);
        assertThat(filter(links, Rel.NEXT).get().limit).isEqualTo(LIMIT);

        assertThat(filter(links, Rel.PREV).get().offset).isEqualTo(OFFSET - LIMIT);
        assertThat(filter(links, Rel.PREV).get().limit).isEqualTo(LIMIT);

        assertThat(filter(links, Rel.FIRST).get().offset).isEqualTo(0);
        assertThat(filter(links, Rel.FIRST).get().limit).isEqualTo(LIMIT);

        assertThat(filter(links, Rel.LAST).get().offset).isEqualTo(OFFSET_FOR_LAST);
        assertThat(filter(links, Rel.LAST).get().limit).isEqualTo(LIMIT);

    }

    @SuppressWarnings("boxing")
    @Test
    public void testThatNoLinksAreCreatedIfLimitIs0() {

        PaginationLinkBuilder paginationLinkBuilder = PaginationLinkBuilder.of(TOTAL, OFFSET, 0);
        final List<Optional<Result>> links = new ArrayList<>();
        createLinks(paginationLinkBuilder, links);

        assertThat(links.size()).isEqualTo(0);
    }

    @Test
    public void testThatNextLinkIsNotCreatedIfNearTheEndOfTheList() {

        PaginationLinkBuilder paginationLinkBuilder = PaginationLinkBuilder.of(TOTAL,
                TOTAL - LIMIT, LIMIT);
        final List<Optional<Result>> links = new ArrayList<>();
        createLinks(paginationLinkBuilder, links);

        assertThat(filter(links, Rel.NEXT)).isEqualTo(Optional.empty());
    }

    @Test
    public void testThatPrevAndFirstAreNotCreatedIfAtTheBeginningOfTheList() {

        PaginationLinkBuilder paginationLinkBuilder = PaginationLinkBuilder.of(TOTAL, 0, LIMIT);
        final List<Optional<Result>> links = new ArrayList<>();
        createLinks(paginationLinkBuilder, links);

        assertThat(filter(links, Rel.FIRST)).isEqualTo(Optional.empty());
        assertThat(filter(links, Rel.PREV)).isEqualTo(Optional.empty());
    }

    @SuppressWarnings("boxing")
    @Test
    public void testThatPrevLinkOffsetIs0IfCloseToTheBeginningOfTheList() {

        PaginationLinkBuilder paginationLinkBuilder = PaginationLinkBuilder.of(TOTAL, LIMIT - 1,
                LIMIT);
        final List<Optional<Result>> links = new ArrayList<>();
        createLinks(paginationLinkBuilder, links);

        assertThat(filter(links, Rel.PREV).get().offset).isEqualTo(0);
    }

    @SuppressWarnings("boxing")
    @Test
    public void testThatOffsetForLastLinkIsCorrectIfLastPageIsCompletelyFilled() {

        PaginationLinkBuilder paginationLinkBuilder = PaginationLinkBuilder.of(TOTAL, 0, LIMIT_2);
        final List<Optional<Result>> links = new ArrayList<>();
        createLinks(paginationLinkBuilder, links);

        assertThat(filter(links, Rel.LAST).get().offset).isEqualTo(TOTAL - LIMIT_2);
    }

    @Test
    public void testThatLastIsNotBuildIfLimitIsLargerThanTotal() {

        PaginationLinkBuilder paginationLinkBuilder = PaginationLinkBuilder.of(TOTAL, 0, TOTAL + 1);
        final List<Optional<Result>> links = new ArrayList<>();
        createLinks(paginationLinkBuilder, links);

        assertThat(filter(links, Rel.LAST)).isEqualTo(Optional.empty());
    }

    private void createLinks(PaginationLinkBuilder paginationLinkBuilder,
            final List<Optional<Result>> links) {
        paginationLinkBuilder.generateLinks((target, off, lim) -> {
            links.add(dummyBuilder.create(target.getRelation(), off, lim));
            return Optional.empty(); // needed to satisfy type constraints
            });
    }

    @SuppressWarnings("boxing")
    private Optional<Result> filter(final List<Optional<Result>> links,
            RelationContainer relationContainer) {

        List<Optional<Result>> filteredLinks = links.stream().filter(
                item -> item.get().target.equals(relationContainer.getRelation())).collect(
                Collectors.toList());

        assertTrue(filteredLinks.size() <= 1);

        return filteredLinks.size() == 1 ? filteredLinks.get(0) : Optional.empty();
    }

    private class DummyBuilder {
        Optional<Result> create(Relation target, int off, int lim) {
            Result result = new Result();
            result.target = target;
            result.offset = off;
            result.limit = lim;
            return Optional.of(result);
        }
    }

    private class Result {
        private Relation target;

        private int offset;

        private int limit;

        public int getOffset() {
            return offset;
        }

        public int getLimit() {
            return limit;
        }
    }
}

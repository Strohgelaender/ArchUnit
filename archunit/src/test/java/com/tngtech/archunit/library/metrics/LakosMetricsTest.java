package com.tngtech.archunit.library.metrics;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.library.metrics.TestMetricsComponentDependencyGraph.fromNode;
import static com.tngtech.archunit.library.metrics.TestMetricsComponentDependencyGraph.graph;
import static com.tngtech.java.junit.dataprovider.DataProviders.$;
import static com.tngtech.java.junit.dataprovider.DataProviders.$$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

@RunWith(DataProviderRunner.class)
public class LakosMetricsTest {

    @Test
    public void lakos_metrics_of_a_single_component() {
        MetricsComponents<TestElement> components = MetricsComponents.of(MetricsComponent.of("component", new TestElement()));

        LakosMetrics metrics = ArchitectureMetrics.lakosMetrics(components);

        assertThat(metrics.getCumulativeComponentDependency())
                .as("CCD").isEqualTo(1);
        assertThat(metrics.getAverageComponentDependency())
                .as("ACD").isEqualTo(1.0);
        assertThat(metrics.getRelativeAverageComponentDependency())
                .as("RACD").isEqualTo(1.0);
        assertThat(metrics.getNormalizedCumulativeComponentDependency())
                .as("NCCD").isEqualTo(1.0);
    }

    @DataProvider
    public static Object[][] dependency_graphs_with_expected_metrics() {
        return $$(
                $(
                        graph(fromNode("A").toNodes("B")),
                        ExpectedMetrics.ccd(3).acd(1.5).racd(0.75).nccd(1)
                ),
                // some binary trees that all have NCCD = 1
                $(
                        graph(fromNode("A").toNodes("B", "C")),
                        ExpectedMetrics.ccd(5).acd(5 / 3.0).racd(5 / 3.0 / 3.0).nccd(1.0)
                ),
                $(
                        graph(fromNode("A").toNodes("B", "C")
                                .fromNode("B").toNodes("D")),
                        ExpectedMetrics.ccd(8).acd(8 / 4.0).racd(8 / 4.0 / 4.0).nccd(1.0)
                ),
                $(
                        graph(fromNode("A").toNodes("B", "C")
                                .fromNode("B").toNodes("D", "E")),
                        ExpectedMetrics.ccd(11).acd(11 / 5.0).racd(11 / 5.0 / 5.0).nccd(1.0)
                ),
                $(
                        graph(fromNode("A").toNodes("B", "C")
                                .fromNode("B").toNodes("D", "E")
                                .fromNode("C").toNodes("F")),
                        ExpectedMetrics.ccd(14).acd(14 / 6.0).racd(14 / 6.0 / 6.0).nccd(1.0)
                ),
                $(
                        graph(fromNode("A").toNodes("B", "C")
                                .fromNode("B").toNodes("D", "E")
                                .fromNode("C").toNodes("F", "G")),
                        ExpectedMetrics.ccd(17).acd(17 / 7.0).racd(17 / 7.0 / 7.0).nccd(1.0)
                ),
                $(
                        graph(fromNode("1").toNodes("21", "22")
                                .fromNode("21").toNodes("311", "312")
                                .fromNode("311").toNodes("4111", "4112")
                                .fromNode("312").toNodes("4121", "4122")
                                .fromNode("22").toNodes("321", "322")
                                .fromNode("321").toNodes("4211", "4212")
                                .fromNode("322").toNodes("4221", "4222")),
                        ExpectedMetrics.ccd(49).acd(49 / 15.0).racd(49 / 15.0 / 15.0).nccd(1.0)
                ),
                // some linear graphs
                $(
                        graph(fromNode("A").toNodes("B")
                                .fromNode("B").toNodes("C")),
                        ExpectedMetrics.ccd(6).acd(2).racd(2 / 3.0).nccd(6 / 5.0)
                ),
                $(
                        graph(fromNode("A").toNodes("B")
                                .fromNode("B").toNodes("C")
                                .fromNode("C").toNodes("D")
                                .fromNode("D").toNodes("E")
                                .fromNode("E").toNodes("F")),
                        ExpectedMetrics.ccd(21).acd(21 / 6.0).racd(21 / 6.0 / 6.0).nccd(21 / 14.0)
                ),
                // some cyclic graphs
                $(
                        graph(fromNode("A").toNodes("B", "Z")
                                .fromNode("B").toNodes("C")
                                .fromNode("C").toNodes("D")
                                .fromNode("D").toNodes("B")
                                .fromNode("Z").toNodes("W")
                                .fromNode("W").toNodes("Z")),
                        ExpectedMetrics.ccd(19).acd(19 / 6.0).racd(19 / 6.0 / 6.0).nccd(19 / 14.0)
                ),
                $(
                        graph(fromNode("A").toNodes("B")
                                .fromNode("B").toNodes("C")
                                .fromNode("C").toNodes("D")
                                .fromNode("D").toNodes("E")
                                .fromNode("E").toNodes("F")
                                .fromNode("F").toNodes("A")),
                        ExpectedMetrics.ccd(36).acd(36 / 6.0).racd(36 / 6.0 / 6.0).nccd(36 / 14.0)
                ),
                $(
                        graph(fromNode("A").toNodes("B", "D")
                                .fromNode("B").toNodes("C")
                                .fromNode("C").toNodes("A")
                                .fromNode("D").toNodes("E")
                                .fromNode("E").toNodes("F")
                                .fromNode("F").toNodes("D")),
                        ExpectedMetrics.ccd(27).acd(27 / 6.0).racd(27 / 6.0 / 6.0).nccd(27 / 14.0)
                ),
                $(
                        graph(fromNode("A").toNodes("B", "D")
                                .fromNode("B").toNodes("C")
                                .fromNode("C").toNodes("A")
                                .fromNode("D").toNodes("E")
                                .fromNode("E").toNodes("F")
                                .fromNode("F").toNodes("D", "B")),
                        ExpectedMetrics.ccd(36).acd(36 / 6.0).racd(36 / 6.0 / 6.0).nccd(36 / 14.0)
                )
        );
    }

    @Test
    @UseDataProvider("dependency_graphs_with_expected_metrics")
    public void lakos_metrics_are_calculated_correctly(TestMetricsComponentDependencyGraph graph, ExpectedMetrics expectedMetrics) {

        LakosMetrics metrics = ArchitectureMetrics.lakosMetrics(graph.toComponents());

        assertThat(metrics.getCumulativeComponentDependency())
                .as("CCD of " + graph).isEqualTo(expectedMetrics.ccd);
        assertThat(metrics.getAverageComponentDependency())
                .as("ACD of " + graph).isEqualTo(expectedMetrics.acd);
        assertThat(metrics.getRelativeAverageComponentDependency())
                .as("RACD of " + graph).isCloseTo(expectedMetrics.racd, offset(0.01));
        assertThat(metrics.getNormalizedCumulativeComponentDependency())
                .as("NCCD of " + graph).isCloseTo(expectedMetrics.nccd, offset(0.01));
    }

    private static class ExpectedMetrics {
        private final int ccd;
        private final double acd;
        private final double racd;
        private final double nccd;

        private ExpectedMetrics(Builder builder) {
            ccd = builder.ccd;
            acd = builder.acd;
            racd = builder.racd;
            nccd = builder.nccd;
        }

        @Override
        public String toString() {
            return "CCD: " + ccd + " / ACD: " + acd + " / RACD: " + racd + " / NCCD: " + nccd;
        }

        static Builder ccd(int ccd) {
            return new Builder(ccd);
        }

        static class Builder {
            private final int ccd;
            private double acd;
            private double racd;
            private double nccd;

            private Builder(int ccd) {
                this.ccd = ccd;
            }

            Builder acd(final double acd) {
                this.acd = acd;
                return this;
            }

            Builder racd(final double racd) {
                this.racd = racd;
                return this;
            }

            ExpectedMetrics nccd(final double nccd) {
                this.nccd = nccd;
                return new ExpectedMetrics(this);
            }
        }
    }
}

# TravelSync – Complexity Analysis (n = text length, m = pattern length)

## M1 – String Algorithms (CO2)
| Algorithm | Purpose in TravelSync | Time | Space |
|---|---|---|---|
| Naive | baseline comparison | O(nm) | O(1) |
| KMP | destination / hotel name search | O(n + m) | O(m) |
| Rabin-Karp | booking code search | O(n + m) avg, O(nm) worst | O(1) |
| Z-Function | repeated phrases in descriptions | O(n) per string | O(n) |
| Aho-Corasick | many keywords at once | O(n + total pattern length + matches) | O(total pattern length) |

## M2 – Suffix Structures (CO2)
| Algorithm | Purpose | Time | Space |
|---|---|---|---|
| Suffix Array (prefix doubling) | index descriptions | O(n log² n) build, O(m log n) query | O(n) |
| Kasai LCP | common descriptions | O(n) | O(n) |
| Suffix Automaton | substring queries, distinct substrings | O(n) build (alphabet constant), O(m) query | O(n) states (at most 2n) |

## M3 – Advanced DP (CO3)
| Algorithm | Purpose | Time | Space |
|---|---|---|---|
| Levenshtein | name correction | O(mn) | O(mn) (O(min(m,n)) with two rows) |
| Damerau-Levenshtein (OSA) | typo correction incl. swaps | O(mn) | O(mn) |
| Bitmask DP | best itinerary | O(2ⁿ · n²) | O(2ⁿ · n) |
| Matrix-Chain | order of data operations | O(n³) | O(n²) |
| Optimal BST | frequent destination lookup | O(n³) (O(n²) with Knuth) | O(n²) |

## Comparisons
- Naive vs KMP vs Rabin-Karp: naive degrades to O(nm) on repetitive text; KMP is O(n + m) in all cases; Rabin-Karp is fast on average but can hit hash collisions.
- Levenshtein vs Damerau: a swap such as "Swtizerland" costs 2 edits in Levenshtein but 1 in Damerau.
- Bitmask DP vs brute force: O(2ⁿ · n²) against O(n!), which is why it stays usable for about 15 destinations.
- Suffix Array vs Suffix Automaton: the array uses less memory and supports fast substring listing; the automaton answers "does it exist" in O(m).
## M4 – Network Flow (CO4)  (V = nodes, E = edges, F = max flow value)
| Algorithm | Purpose in TravelSync | Time | Space |
|---|---|---|---|
| Ford-Fulkerson (DFS) | passenger flow through transport links | O(E · F) | O(V + E) |
| Edmonds-Karp (BFS) | same, polynomial bound | O(V · E²) | O(V + E) |
| Dinic | larger transport networks | O(V² · E) | O(V + E) |
| Min-cut | find transport bottlenecks | one max-flow + O(V + E) | O(V + E) |
| Bipartite matching via max-flow | travellers to hotel rooms | O(E · √V) with Dinic on unit capacities | O(V + E) |

## M5 – SAT and Graph Reductions (CO5)
| Algorithm | Purpose in TravelSync | Time | Space |
|---|---|---|---|
| 3-SAT assignment checker | Check a proposed set of travel preferences against clauses | O(C) | O(1) extra |
| 3-SAT → CLIQUE | One vertex per literal occurrence; connect compatible occurrences from different clauses | O(C²) | O(C²) |
| CLIQUE → INDEPENDENT-SET | Complement the graph; target size remains unchanged | O(V²) | O(V²) |
| INDEPENDENT-SET → VERTEX-COVER | Same graph; target size becomes V − k | O(1) after graph construction | O(1) |
| Matching-based vertex-cover 2-approximation | Select both endpoints of every greedily selected disjoint edge | O(V + E) | O(V + E) |

The 3-SAT demo uses three travel-choice variables and a small 3-CNF formula. The reductions demonstrate the graph constructions. The approximation guarantees a cover no larger than twice the optimum; it is a heuristic and does not claim to find a minimum cover.

## M6 – Randomized, Streaming and Parallel Algorithms (CO5)
| Algorithm | Purpose in TravelSync | Time | Space |
|---|---|---|---|
| Randomized QuickSort | Rank booking/popularity scores | Expected O(n log n), worst O(n²) | Expected O(log n) stack |
| Reservoir sampling | Keep a uniform sample from a one-pass booking-event stream | O(n) | O(1) |
| Miller–Rabin | Probabilistic primality demonstration for large integer IDs | O(k log n · log n) modular operations | O(1) |
| Blelloch exclusive scan | Prefix totals for batches of travel events | O(n) work | O(n) |
| Parallel reduce | Aggregate batch totals across worker threads | O(n) work; span O(n/P + log P) for balanced chunks | O(P) auxiliary |

The menu's small inputs are demonstrations, not performance measurements. The benchmark menu exports its suite runtime, while transport flow records each algorithm comparison in `output/flow_results.csv`.

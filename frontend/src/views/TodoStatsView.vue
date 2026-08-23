<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { TodoStats } from '@/types/todoStats';
import { getTodoStats } from '@/services/apiService';
import { showToast, Toast } from '@/ts/toasts';
import { Button } from 'agnostic-vue';

const stats = ref<TodoStats | null>(null);
const isLoading = ref<boolean>(true);

// Fixed palette for the category pie slices; keys are assigned in insertion
// order so colors stay stable between reloads with the same data shape.
const PIE_COLORS = ['#009ee2', '#f5a623', '#7ed321', '#9013fe', '#d0021b', '#4a90d9'];

// Bar-chart geometry: the SVG viewBox the bars are laid out in.
const BAR_WIDTH = 480;
const BAR_HEIGHT = 200;

const priorityEntries = computed(() => Object.entries(stats.value?.todosPerPriority ?? {}));
const categoryEntries = computed(() => Object.entries(stats.value?.todosPerCategory ?? {}));
const assigneeEntries = computed(() => stats.value?.todosPerAssignee ?? []);

const maxPriorityCount = computed(() => Math.max(1, ...priorityEntries.value.map(([, c]) => c)));
const maxAssigneeCount = computed(() => Math.max(1, ...assigneeEntries.value.map((a) => a.count)));

/**
 * Computes SVG arc path data for one pie slice.
 *
 * @param cx - center x
 * @param cy - center y
 * @param r - radius
 * @param startAngle - slice start in radians
 * @param endAngle - slice end in radians
 * @returns an SVG `d` attribute describing the wedge
 */
function pieSlicePath(cx: number, cy: number, r: number, startAngle: number, endAngle: number): string {
  // Full-circle case would collapse to a zero-length arc; draw two halves instead.
  if (endAngle - startAngle >= Math.PI * 2 - 1e-6) {
    return `M ${cx} ${cy} L ${cx + r} ${cy} A ${r} ${r} 0 1 1 ${cx - r} ${cy} A ${r} ${r} 0 1 1 ${cx + r} ${cy} Z`;
  }
  const x1 = cx + r * Math.cos(startAngle);
  const y1 = cy + r * Math.sin(startAngle);
  const x2 = cx + r * Math.cos(endAngle);
  const y2 = cy + r * Math.sin(endAngle);
  const largeArc = endAngle - startAngle > Math.PI ? 1 : 0;
  return `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${largeArc} 1 ${x2} ${y2} Z`;
}

const pieSlices = computed(() => {
  const total = categoryEntries.value.reduce((sum, [, count]) => sum + count, 0);
  let angle = -Math.PI / 2; // start at 12 o'clock
  return categoryEntries.value.map(([label, count], index) => {
    const sweep = total === 0 ? 0 : (count / total) * Math.PI * 2;
    const path = pieSlicePath(100, 100, 80, angle, angle + sweep);
    const start = angle;
    angle += sweep;
    const mid = start + sweep / 2;
    return {
      label,
      count,
      percent: total === 0 ? 0 : Math.round((count / total) * 100),
      path,
      color: PIE_COLORS[index % PIE_COLORS.length],
      labelX: 100 + 95 * Math.cos(mid),
      labelY: 100 + 95 * Math.sin(mid),
    };
  });
});

// Fetches the statistics from the backend and clears the loading state.
async function fetchStats() {
  isLoading.value = true;
  try {
    stats.value = await getTodoStats();
  } catch (error) {
    console.error('Error fetching todo stats:', error);
    showToast(new Toast('Error', 'Failed to load statistics.', 'error'));
  } finally {
    isLoading.value = false;
  }
}

onMounted(fetchStats);
</script>

<template>
  <div class="stats-view">
    <div class="view-header">
      <h1 class="heading">Statistics</h1>
      <Button mode="secondary" @click="fetchStats" :disabled="isLoading">
        {{ isLoading ? 'Loading...' : 'Refresh' }}
      </Button>
    </div>

    <div v-if="isLoading && !stats" class="loading-message card">Loading statistics...</div>

    <template v-else-if="stats">
      <!-- Headline numbers -->
      <div class="summary-cards">
        <div class="card summary-card">
          <p class="summary-value">{{ stats.totalTodos }}</p>
          <p class="summary-label">Total todos</p>
        </div>
        <div class="card summary-card">
          <p class="summary-value">{{ stats.finishedTodos }}</p>
          <p class="summary-label">Finished</p>
        </div>
        <div class="card summary-card">
          <p class="summary-value">{{ stats.completionRate }}%</p>
          <p class="summary-label">Completion rate</p>
        </div>
        <div class="card summary-card">
          <p class="summary-value">
            {{ stats.averageDaysToFinish === null ? '—' : stats.averageDaysToFinish.toFixed(1) }}
          </p>
          <p class="summary-label">Avg. days to finish</p>
        </div>
      </div>

      <div class="charts-grid">
        <!-- Todos per assignee: horizontal bar chart -->
        <section class="card chart-card">
          <h2 class="chart-title">Todos per Assignee</h2>
          <svg
            v-if="assigneeEntries.length > 0"
            :viewBox="`0 0 ${BAR_WIDTH} ${Math.max(BAR_HEIGHT, assigneeEntries.length * 36)}`"
            class="chart-svg"
            role="img"
            aria-label="Bar chart of todos per assignee"
          >
            <g v-for="(entry, index) in assigneeEntries" :key="entry.assigneeId">
              <text :x="0" :y="index * 36 + 16" class="axis-label">
                {{ entry.prename }} {{ entry.name }}
              </text>
              <rect
                :x="150"
                :y="index * 36 + 4"
                :width="(entry.count / maxAssigneeCount) * (BAR_WIDTH - 190)"
                height="20"
                rx="4"
                fill="#009ee2"
              />
              <text :x="156 + (entry.count / maxAssigneeCount) * (BAR_WIDTH - 190)" :y="index * 36 + 19" class="bar-value">
                {{ entry.count }}
              </text>
            </g>
          </svg>
          <p v-else class="chart-empty">No assigned todos yet.</p>
        </section>

        <!-- Todos per priority: horizontal bar chart -->
        <section class="card chart-card">
          <h2 class="chart-title">Todos per Priority</h2>
          <svg
            v-if="priorityEntries.length > 0"
            :viewBox="`0 0 ${BAR_WIDTH} ${Math.max(BAR_HEIGHT, priorityEntries.length * 44)}`"
            class="chart-svg"
            role="img"
            aria-label="Bar chart of todos per priority"
          >
            <g v-for="(entry, index) in priorityEntries" :key="entry[0]">
              <text :x="0" :y="index * 44 + 18" class="axis-label">{{ entry[0] }}</text>
              <rect
                :x="110"
                :y="index * 44 + 4"
                :width="(entry[1] / maxPriorityCount) * (BAR_WIDTH - 160)"
                height="26"
                rx="4"
                :fill="['#d0021b', '#f5a623', '#7ed321'][index % 3]"
              />
              <text :x="118 + (entry[1] / maxPriorityCount) * (BAR_WIDTH - 160)" :y="index * 44 + 22" class="bar-value">
                {{ entry[1] }}
              </text>
            </g>
          </svg>
          <p v-else class="chart-empty">No todos yet.</p>
        </section>

        <!-- Todos per category: pie chart -->
        <section class="card chart-card">
          <h2 class="chart-title">Todos per Category</h2>
          <svg
            v-if="pieSlices.length > 0"
            viewBox="0 0 200 200"
            class="pie-svg"
            role="img"
            aria-label="Pie chart of todos per category"
          >
            <path v-for="slice in pieSlices" :key="slice.label" :d="slice.path" :fill="slice.color">
              <title>{{ slice.label }}: {{ slice.count }}</title>
            </path>
            <text
              v-for="slice in pieSlices.filter((s) => s.percent >= 10)"
              :key="`label-${slice.label}`"
              :x="slice.labelX"
              :y="slice.labelY"
              text-anchor="middle"
              class="pie-label"
            >
              {{ slice.percent }}%
            </text>
          </svg>
          <p v-else class="chart-empty">No categorized todos yet.</p>
          <ul v-if="pieSlices.length > 0" class="pie-legend">
            <li v-for="slice in pieSlices" :key="`legend-${slice.label}`">
              <span class="legend-swatch" :style="{ backgroundColor: slice.color }"></span>
              {{ slice.label }} ({{ slice.count }})
            </li>
          </ul>
        </section>
      </div>
    </template>
  </div>
</template>

<style scoped>
.stats-view {
  width: 100%;
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-xl);
}

.loading-message {
  text-align: center;
  padding: var(--space-xl);
  color: var(--color-text-light);
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: var(--space-md);
  margin-bottom: var(--space-xl);
}

.summary-card {
  padding: var(--space-lg);
  text-align: center;
}

.summary-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-primary);
}

.summary-label {
  color: var(--color-text-light);
  font-size: 0.85rem;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: var(--space-lg);
}

.chart-card {
  padding: var(--space-lg);
}

.chart-title {
  font-size: 1.05rem;
  font-weight: 600;
  margin-bottom: var(--space-md);
  color: var(--color-text);
}

.chart-svg {
  width: 100%;
  height: auto;
}

.pie-svg {
  width: 220px;
  height: 220px;
  margin: 0 auto;
  display: block;
}

.axis-label {
  font-size: 12px;
  fill: var(--color-text);
}

.bar-value {
  font-size: 12px;
  font-weight: 600;
  fill: var(--color-text);
}

.pie-label {
  font-size: 11px;
  font-weight: 600;
  fill: white;
  pointer-events: none;
}

.chart-empty {
  color: var(--color-text-light);
  text-align: center;
  padding: var(--space-lg);
}

.pie-legend {
  list-style: none;
  padding: 0;
  margin-top: var(--space-md);
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-md);
  justify-content: center;
  font-size: 0.85rem;
  color: var(--color-text);
}

.legend-swatch {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 2px;
  margin-right: 6px;
}
</style>

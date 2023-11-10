<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  -->
  <template>
    <div class="hackit-debugger">
      <div v-if="isLoading">Loading...</div>
      <div v-else>
        <div class="filters mt-1 p-3">
          <div class="row justify-content-center">
            <!-- Adjust column sizes for a narrower layout -->
            <div class="col-md-6 col-lg-4 d-flex justify-content-center">
              <div class="text-center">
                <label for="task-select" class="form-label">Filter by Task ID</label>
                <select id="task-select" class="form-select" v-model="selectedTask">
                  <option value="">All</option>
                  <option v-for="taskId in taskIds" :key="taskId" :value="taskId">
                    {{ taskId }}
                  </option>
                </select>
              </div>
            </div>
            <div class="col-md-6 col-lg-4 d-flex justify-content-center">
              <div class="text-center">
                <label for="tag-select" class="form-label">Filter by Tag</label>
                <select id="tag-select" class="form-select" v-model="selectedTag">
                  <option value="">All</option>
                  <option v-for="tag in uniqueTags" :key="tag" :value="tag">
                    {{ tag }}
                  </option>
                </select>
              </div>
            </div>
          </div>
        </div>
        <div class="table-responsive">
          <table class="table table-borderless mb-0" ref="hackitTable">
            <thead>
              <tr>
                <th class="text-center">Tuple ID</th>
                <th class="text-center">HackIT Tags</th>
              </tr>
            </thead>
            <tbody v-if="filteredTuples.length > 0">
              <Tuple v-for="(tuple, index) in filteredTuples" :key="index" :tuple="tuple" />
            </tbody>
          </table>
        </div>
        <div v-if="filteredTuples.length === 0" class="alert alert-warning mt-3" role="alert">
          <h6 class="p-2">No tuples available in this task</h6>
        </div>
      </div>
    </div>
  </template>
  
  

<script>
import Tuple from "./Tuple.vue";
import $ from 'jquery';
import 'datatables.net';
import 'datatables.net-dt/css/jquery.dataTables.min.css';

export default {
  name: "HackitDebugger",
  components: {
    Tuple,
  },

  props: {
    jobId: {
      type: String,
      required: true,
    },
    taskId: {
      type: String,
      required: true,
    },
    hackitAction: {
      type: String,
      required: false,
    },
  },

  data() {
    return {
      isLoading: true,
      tuples: [],
      taskIds: [],
      uniqueTags: [],
      selectedTask: "",
      selectedTag: "",
    };
  },

  created() {
    this.fetchTuples();
    this.addTestTuples();
  },

  watch: {
    taskId: function () {
      this.filterTuples();
    },
    hackitAction: function () {
      this.hackitActionRun();
    },
  },

  computed: {
    filteredTuples() {
      let filtered = this.tuples;

      if (this.selectedTask) {
        filtered = filtered.filter(
          (tuple) => tuple.task_id === this.selectedTask
        );
      }

      if (this.selectedTag) {
        filtered = filtered.filter((tuple) =>
          tuple.hackit_tuple.metadata.tags.includes(this.selectedTag)
        );
      }

      return filtered;
    },

    taskIds() {
      const taskIds = new Set();
      this.tuples.forEach((tuple) => {
        taskIds.add(tuple.task_id);
      });
      return Array.from(taskIds);
    },
    uniqueTags() {
      const tags = new Set();
      this.tuples.forEach((tuple) => {
        tuple.hackit_tuple.metadata.tags.forEach((tag) => tags.add(tag));
      });
      return Array.from(tags);
    },
  },

  mounted() {
    this.fetchTuples();
  },

  methods: {

    initializeDataTables() {
    this.$nextTick(() => {
      // Destroy existing DataTables instance to reinitialize
      if ($.fn.dataTable.isDataTable(this.$refs.hackitTable)) {
        $(this.$refs.hackitTable).DataTable().destroy();
      }

      // Initialize DataTables with lengthChange set to false
      $(this.$refs.hackitTable).DataTable({
        responsive: true,
        lengthChange: false, 
        searching:false,
        // This will hide the 'Show entries' feature
        // ... other DataTable options ...
      });
    });
  },
    async fetchTuples() {
      try {
        const response = await fetch(
          `http://localhost:3000/tuples/?job_id=${this.jobId}`
        );
        this.tuples = await response.json();
        this.updateTaskIdsAndTags();
        this.filterTuples();
      } catch (error) {
        console.error("Error fetching tuples:", error);
      } finally {
        this.isLoading = false;
        this.initializeDataTables();
      }
    },

    debugTuple(tuple) {
      // Debug the tuple
      // Assuming this is where you identify the debuggable tuple
      const debuggableTuple = {
        /* tuple data */
      };
      // Emit an event to notify the parent component
      this.$emit("debuggable-tuple", debuggableTuple);
    },
    updateTaskIdsAndTags() {
      const taskIdsSet = new Set();
      const tagsSet = new Set();

      this.tuples.forEach((tuple) => {
        taskIdsSet.add(tuple.task_id);
        tuple.hackit_tuple.metadata.tags.forEach((tag) => tagsSet.add(tag));
      });

      this.taskIds = Array.from(taskIdsSet);
      this.uniqueTags = Array.from(tagsSet);
    },
    hackitActionRun() {
      switch (this.hackitAction) {
        case "nextTuple":
          this.createNewNextTuple();
          break;
        case "":
          break;
        default:
          break;
      }
    },
    addTestTuples() {
      for (let i = 0; i < 50; i++) {
        const testTuple = {
          task_id: `TestTask${i}`,
          hackit_tuple: {
            metadata: {
              tuple_id: `TestTuple${i}`,
              tags: ["testTag1", "testTag2"],
            },
          },
        };
        this.tuples.push(testTuple);
      }
      this.tuples = [...this.tuples];
    },
    createNewNextTuple() {
      const tempTuples = this.filteredTuples;
      const tempTuple = JSON.parse(
        JSON.stringify(tempTuples[tempTuples.length - 1])
      );
      tempTuple.hackit_tuple.metadata.tuple_id =
        tempTuples[tempTuples.length - 1].hackit_tuple.metadata.tuple_id + 1;
      this.tuples.push(tempTuple);
    },
    filterTuples() {
      this.selectedTask = this.taskId;
      this.initializeDataTables();
    },
  },
};
</script>
<style scoped>
.table-responsive {
  max-width: 90%;  
  margin: auto;   
}
.tuple-cell {
  text-align: center;
}
</style>

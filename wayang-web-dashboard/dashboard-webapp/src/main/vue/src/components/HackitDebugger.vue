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
  <!---Tuples - Filter by task ID-->
  
  <div class="hackit-debugger">
    <div v-if="isLoading">Loading...</div>
    <div v-else>
      <div class="filters mt-1 p-3">
        <label for="task-select" class="p-2">Filter by Task ID: </label>
        <select id="task-select" v-model="selectedTask">
          <option value="">All</option>
          <option v-for="taskId in taskIds" :key="taskId" :value="taskId">
            {{ taskId }}
          </option>
        </select>
        <label for="tag-select" class="p-2">Filter by Tag:</label>
        <select id="tag-select" v-model="selectedTag">
          <option value="">All</option>
          <option v-for="tag in uniqueTags" :key="tag" :value="tag">
            {{ tag }}
          </option>
        </select>


      </div>
      <table class="table table-borderless mb-0">
        <table class="table table-borderless mb-0">
          <thead>
            <tr>
              <th class="col-4">Tuple ID</th>
              <th style="text-align: left" class="col-6">HackIT Tags</th>
              <th class="text-center col-2"></th>
            </tr>
          </thead>
        </table>
      </table>
      <div class="table-body-container overflow-auto" style="max-height: 400px">
        <table class="table table-borderless mb-0">
          <tbody v-if="filteredTuples.length > 0">
            <Tuple v-for="(tuple, index) in filteredTuples" :key="index" :tuple="tuple" />

            
          </tbody>
        </table>
      </div>
      <div class="col-6 p-2">
        <div class="alert alert-warning mt-3 px-1 text-left" style="width: 1180px" role="alert"
          v-if="filteredTuples.length === 0">
          <h6 class="p-2">No tuples available in this task</h6>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Tuple from "./Tuple.vue";

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
  methods: {
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
    },
  },
};
</script>
<style scoped></style>

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
  <div class="hackit-controls" style="display: flex; align-items: center">
    <button class="btn btn-secondary" style="margin-right: 10px; padding: 5px 20px" :class="{ 'btn-dark': isPaused }"
      :disabled="!isPaused" @click="onPause">
      Pause
    </button>
    <button class="btn btn-secondary" style="margin-right: 10px; white-space: nowrap; padding: 5px 20px"
      :class="{ 'btn-dark': isVirtualPaused }" :disabled="!isVirtualPaused" @click="onVirtualPause">
      Virtual Pause
    </button>
    <button class="btn btn-secondary" style="margin-right: 10px; padding: 5px 20px" :class="{ 'btn-dark': isRunning }"
      :disabled="!isRunning" @click="onPlay">
      Resume
    </button>
    <button class="btn btn-secondary" style="margin-right: 10px; white-space: nowrap; padding: 5px 20px"
      :class="{ 'btn-dark': isNextDebugTupleActive }" :disabled="!isNextDebugTupleActive" @click="onNextDebugTuple">
      Next Debug Tuple
    </button>
    <button class="btn btn-secondary" style="margin-right: 10px; white-space: nowrap; padding: 5px 20px"
      :class="{ 'btn-dark': isNextTupleActive }" :disabled="!isNextTupleActive" @click="onNextTuple">
      Next Tuple
    </button>

    <button class="btn btn-secondary" style="margin-right: 10px; white-space: nowrap; padding: 5px 20px"
      :class="{ 'btn-dark': isNextOperatorActive }" :disabled="!isNextOperatorActive" @click="nextOperatorClicked">
      Next Operator
    </button>
  </div>
</template>

<script>
import Codemirror from "codemirror-editor-vue3";
import JobPlan from "./JobPlan.vue";

export default {
  name: "HackitControls",
  components: {
    Codemirror,
    JobPlan,
  },

  props: {
    nextNodeId: String,
  },
  data() {
    return {
      isRunning: false,
      isPaused: true,
      isVirtualPaused: true,
      isNextTupleActive: false,
      isNextOperatorActive: false,
      isNextDebugTupleActive: false,
    };
  },
  methods: {
    onPlay() {
      if (this.isRunning) {
        this.$emit("play", "isRunning");
        this.isRunning = false;
        this.isPaused = true;
        this.isVirtualPaused = true;
        this.isNextOperatorActive = false;
        this.isNextTupleActive = false;
        this.isNextDebugTupleActive = false;
      }
    },
    onPause() {
      if (this.isPaused) {
        this.$emit("pause", "isPaused");
        this.isRunning = true;
        this.isPaused = false;
        this.isVirtualPaused = true;
        this.isNextOperatorActive = true;
        this.isNextTupleActive = true;
      }
    },
    onVirtualPause() {
      if (this.isVirtualPaused) {
        this.$emit("virtual-pause", "isVirtualPaused");
        this.isRunning = true;
        this.isVirtualPaused = false;
        this.isPaused = false;
        this.isNextOperatorActive = true;
        this.isNextTupleActive = false;
        this.isNextDebugTupleActive = true;
        this.isNextOperatorActive = false;
      }
    },
    onNextDebugTuple() {
      if (this.isNextDebugTupleActive) {
        this.$emit("next-debug-tuple", "isNextDebugTupleActive");
      }
    },
    onNextTuple() {
      if (this.isNextTupleActive) {
        this.$emit("next-tuple", "isNextTupleActive");
      }
    },
    nextOperatorClicked() {
      if (this.isNextOperatorActive) {
        this.$emit("next-operator-clicked", "isNextOperatorActive");
      }
    },
    async pauseJob() {
      try {
        const response = await axios.post("http://localhost:3000/pause-job");
        return response.data;
      } catch (error) {
        throw new Error("Failed to pause job");
      }
    },

    async virtualPauseJob() {
      try {
        const response = await axios.post(
          "http://localhost:3000/virtual-pause-job"
        );
        return response.data;
      } catch (error) {
        throw new Error("Failed to virtual pause job");
      }
    },

    async resumeJob() {
      try {
        const response = await axios.post("http://localhost:3000/resume-job");
        return response.data;
      } catch (error) {
        throw new Error("Failed to resume job");
      }
    },

    async getNextDebugTuple() {
      try {
        const response = await axios.get(
          "http://localhost:3000/next-debug-tuple"
        );
        return response.data;
      } catch (error) {
        throw new Error("Failed to get the next debug tuple");
      }
    },

    async getNextTuple() {
      try {
        const response = await axios.get("http://localhost:3000/next-tuple");
        return response.data;
      } catch (error) {
        throw new Error("Failed to get the next tuple");
      }
    },

    async getNextOperator() {
      try {
        const response = await axios.get("http://localhost:3000/next-operator");
        return response.data;
      } catch (error) {
        throw new Error("Failed to get the next operator");
      }
    },
  },
};
</script>

<style scoped></style>

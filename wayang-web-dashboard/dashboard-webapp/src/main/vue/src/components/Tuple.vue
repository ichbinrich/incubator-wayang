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
<!-- Inside the Tuple component template -->
<template>
  <tr class="tuple">
    <td class="text-right pr-4" style="padding-right: 10px;">{{ tuple.hackit_tuple.metadata.tuple_id }}</td>
    <td>
      <div class="d-flex">
        <div class="ml-auto">
          <i v-if="tuple.hackit_tuple.metadata.tags.includes('MONITOR')" class="fas fa-magnifying-glass"
            style="margin-left: -30px;" title="Monitor"></i>
          <i v-if="tuple.hackit_tuple.metadata.tags.includes('DEBUG')" class="fas fa-bug red-icon text-danger"
            style="margin-left: 5px;" title="Debug"></i>
        </div>
      </div>
    </td>
    <td>
      <tuple-inspect-modal :tuple="tuple"></tuple-inspect-modal>
    </td>
  </tr>
</template>
<script>
import TupleInspectModal from "@/components/TupleInspectModal.vue";

export default {
  name: "Tuple",

  props: {
    tuple: Object,
  },
  data() {
    return {

    };
  },
  methods: {
    formatTimestamp(timestamp) {
      const date = new Date(timestamp);
      return date.toLocaleString();
    },
  },
  addTag() {
    // Emit an event to add a tag
    this.$emit("add-tag");
    if (this.debug && !this.isBugTupleAlreadyAdded(this.tuple)) {
      useTupleStore.addDebugTuple(this.tuple); // Add the tuple to debugTuples in the store
    }
  },
  selectTag(tag) {
    // Handle selecting a tag
    if (this.selectedTags.includes(tag)) {
      // If the tag is already selected, remove it
      this.selectedTags = this.selectedTags.filter((selectedTag) => selectedTag !== tag);
    } else {
      // Otherwise, add it to the selectedTags array
      this.selectedTags.push(tag);
    }
  },
  components: {
    TupleInspectModal,
  },
  computed: {
    debug: function () {
      return this.tuple.hackit_tuple.metadata.tags.includes("DEBUG");
    },
  },
};
</script>

<style scoped></style>
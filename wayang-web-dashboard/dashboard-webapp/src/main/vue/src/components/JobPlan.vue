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
  <div class="container-fluid my-3">
    <!-- Graph Display -->
    <div id="cy"></div>
    <!-- Wayang Programming Languages -->
    <div class="row mb-3">
      <div class="col-12 col-md-8 d-flex flex-wrap align-items-center">
        <div class="d-flex align-items-center mb-1">
          <span class="bg-warning rounded me-2" style="width: 20px; height: 20px;"></span>
          <div :class="{ 'text-secondary opacity-50': !isJavaPresent }">Java</div>
        </div>
        <div class="d-flex align-items-center mb-1 mx-2">
          <span class="bg-primary rounded me-2" style="width: 20px; height: 20px;"></span>
          <div :class="{ 'text-secondary opacity-50': isSparkPresent }">Spark</div>
        </div>
        <div class="d-flex align-items-center mb-1">
          <span class="bg-success rounded me-2" style="width: 20px; height: 20px;"></span>
          <div :class="{ 'text-secondary opacity-50': isPostgreSQLPresent }">PostgreSQL</div>
        </div>
      </div>
    </div>
    <!-- Github repository -->
    <div class="row mb-3 align-items-center">
      <div class="col-12 col-lg-6 col-xl-5">
        <div class="input-group">
          <span class="input-group-text"><i class="fab fa-github"></i></span>
          <input type="url" v-model="githubRepoURL" placeholder="https://github.com/your-repo" class="form-control" />
          <button @click="submitRepoURL" :disabled="isSubmitting" class="btn btn-secondary">Load</button>
        </div>
        <p>{{ submissionMessage }}</p>
      </div>
    </div>
    <!-- Codemirror IDE -->
    <div class="row mb-2">
      <div class="col">
        <Codemirror id="your-unique-codemirror" ref="codeMirror" v-model:value="codeContent" :options="cmOptions" border
          placeholder="Write your code here..." :style="{ height: '200px', fontSize: '17px' }" @change="change" />
      </div>
    </div>
    <div class="d-flex justify-content-end">
      <button type="button" class="btn btn-dark me-2" @click="saveCode">
        Save
      </button>
      <button type="button" class="btn btn-primary" @click="executeCode">
        <i class="fa fa-play"></i> Run
      </button>
    </div>

    <!-- Tags -->
    <div class="mt-3">
      <h6>Select Predefined Tags</h6>
      <div class="d-flex flex-wrap gap-2">
        <div class="form-check" v-for="(tag, index) in tags" :key="index">
          <input type="checkbox" class="form-check-input" :id="`tag-${index}`" :value="tag" v-model="selectedTags" />
          <label class="form-check-label" :for="`tag-${index}`">{{ tag }}</label>
        </div>
      </div>
    </div>
  </div>
  <!-- Modal -->
  <div v-if="isModalVisible" class="modal" tabindex="-1" role="dialog" style="display: block;">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Code Saved</h5>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close" @click="toggleModal(false)">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <p>Your code has been successfully saved.</p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="toggleModal(false)">Close</button>
        </div>
      </div>
    </div>
  </div>
<!-- Bootstrap Modal -->
<div class="modal fade" id="tagAlertModal" tabindex="-1" role="dialog" aria-labelledby="tagAlertModalLabel" aria-hidden="true">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="tagAlertModalLabel">Alert</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        Please select at least one tag before running the code.
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
      </div>
    </div>
  </div>
</div>

</template>
  
<script>
import cytoscape from "cytoscape";
import contextMenus from "cytoscape-context-menus";
import "cytoscape-context-menus/cytoscape-context-menus.css";
import { ref } from "vue";
import Codemirror from "codemirror-editor-vue3";
import "codemirror/addon/display/placeholder.js";
import "codemirror/addon/display/placeholder.js";
import "codemirror/theme/dracula.css";
import "codemirror/theme/ambiance.css";
import "codemirror/theme/eclipse.css";
import "codemirror/theme/blackboard.css";
import codemirror from "codemirror";
import "codemirror/mode/clike/clike.js";
import "codemirror/mode/sql/sql.js";
import hljs from "highlight.js";
import "highlight.js/styles/default.css";
import "highlight.js/lib/languages/java";
import "highlight.js/lib/languages/sql";
import "highlight.js/lib/languages/scala";

cytoscape.use(contextMenus);

export default {
  name: "JobPlan",
  components: {
    codemirror,
  },
  props: {
    graph: Object,
    task_id: String,
    code: String,

  },

  data() {
    return {
      selectedTaskId: null,
      modalTitle: "",
      codeContent: "",
      tags: ["Alert", "Debug", "Pause", "Skip"],
      selectedTags: [],
      githubRepoURL: "",
      submissionMessage: "",
      isModalVisible: false,
      isSubmitting: false,
      nodeStatus: {
        java: {
          available: true,
          color: 'yellow'
        },
        spark: {
          available: true,
          color: 'blue'
        },
        postgreSQL: {
          available: true,
          color: 'green'
        },
      },
      currentLanguage: "java",
      cmOptions: {
        mode: "text/x-java",
        theme: "default",
        lineNumbers: true,
      },
    };
  },
  computed: {
    // These will depend on your actual data structure
    isJavaPresent() {
      return this.graph.nodes.some(node => node.id === 'java' || node.type === 'java');
    },
    isSparkPresent() {
      return this.graph.nodes.some(node => node.id === 'spark' || node.type === 'spark');
    },
    isPostgreSQLPresent() {
      return this.graph.nodes.some(node => node.id === 'postgresql' || node.type === 'postgresql');
    },
  },

  mounted() {
    const colors = ["green", "blue", "blue", "green", "green"];

    const elements = {
      nodes: this.graph.nodes.map((node, index) => {
        const color = colors[index % colors.length];
        return { ...node, style: { "background-color": color } };
      }),
      edges: this.graph.edges,
    };
    const cy = cytoscape({
      container: document.getElementById("cy"),
      elements: elements,
      style: [
        {
          selector: "node",
          style: {
            "background-color": "#666",
            label: "data(label)",
            width: "20px",
            height: "20px",
            "font-size": "10px",
            "text-valign": "top",
            "text-halign": "center",
            "text-margin-y": "-10px",
          },
        },
        {
          selector: "edge",
          style: {
            width: 3,
            "line-color": "#ccc",
            "target-arrow-color": "#ccc",
            "target-arrow-shape": "triangle",
          },
        },
        {
          selector: ".selected",
          style: {
            "border-color": "red",
            "border-width": "3px",
          },
        },
      ],
      layout: {
        name: "grid",
        fit: true,
        padding: 30,
        animate: false,
        avoidOverlap: true,
      },
      userZoomingEnabled: false,
      userPanningEnabled: false,
      autoungrabify: true,
    });

    const selectNode = (task_id) => {
      const node = cy.nodes(`[task_id="${task_id}"]`);
      if (node.length > 0) {
        node.addClass("selected");
      }
    };

    cy.on("tap", "node", (event) => {
      if (event.target.hasClass("selected")) {
        event.target.removeClass("selected");
        this.$emit("task-selected", null);
      } else {
        this.clearCodeMirror(); // Clear the CodeMirror content
        cy.nodes().removeClass("selected");
        event.target.addClass("selected");
        this.$emit("task-selected", event.target.data("task_id"));
      }
    });

    this.$eventBus.on("next-operator-clicked", () => {

      this.selectedNodeIndex++;

      // Ensure the index stays within bounds
      if (this.selectedNodeIndex >= cy.nodes().length) {
        this.selectedNodeIndex = 0;
      }

      // Deselect all nodes
      cy.nodes().unselect();

      // Select the next node operator
      const selectedNode = cy.nodes()[this.selectedNodeIndex];
      selectedNode.select();

      // You can add your custom logic here to respond to the button click.
    });

    cy.contextMenus({
      menuItems: [
        {
          id: "add-pretag",
          content: "Add PreTAG",
          selector: "node",
          onClickFunction: (event) => {
            this.showCodeEditor("PreTAG");
          },
          hasTrailingDivider: true,
        },


        {
          id: "add-posttag",
          content: "Add PostTAG",
          selector: "node",
          onClickFunction: (event) => {
            this.showCodeEditor("PostTAG");
          },
        },
      ],
    });
    // Call hljs.highlightAll() after everything is initialized
    hljs.highlightAll();
  },

  methods: {

    showCodeEditor(tagType) {
      this.showModal = true;
      this.modalTitle = `Run Selected tag : ${tagType}`;

      const placeholderCode = ` \n`;

      if (tagType === "PreTAG") {
        this.codeContent = `public void pretag() {\n${placeholderCode}\n}`;
      } else if (tagType === "PostTAG") {
        this.codeContent = `public void postag() {\n${placeholderCode}\n}`;
      }
    },
    onModeChange() {
      this.cmOptions.mode = this.mode;
      if (this.mode === "text/x-java") {
        this.currentLanguage = "java";
        this.codeContent = `public void methodName() {\n  // Java code here\n}`;
      } else if (this.mode === "text/x-sql") {
        this.currentLanguage = "sql";
        this.codeContent = `SELECT * FROM tableName;`;
      } else if (this.mode === "text/x-scala") {
        this.currentLanguage = "scala";
        this.codeContent = `def sparkFunction(): Unit = {\n  // Spark Scala code here\n}`;
      }
    },

    nextOperatorClicked() {
      if (this.isNextOperatorActive) {
        // Clear the CodeMirror content
        this.clearCodeMirror();

        // Emit the 'next-operator-clicked' event to the event bus
        this.$eventBus.emit("next-operator-clicked");
      }
    },


    executeCode() {
      console.log("Running code...");
    },

    closeModal() {
      this.showModal = false;
    },
    saveCode() {
      // Check if any tags are selected
      if (this.selectedTags.length === 0) {
        alert('Please select at least one tag before saving.');
        return; // Exit the function if no tags are selected
      }

      // Log the code and tags to the console
      console.log('Saving code:', this.codeContent);
      console.log('With tags:', this.selectedTags);

      // Save the code and tags to localStorage
      localStorage.setItem('userCode', this.codeContent);
      localStorage.setItem('userTags', JSON.stringify(this.selectedTags));

      // Show the success modal
      this.toggleModal(true);

      // Optional: Automatically hide the modal after 3 seconds
      setTimeout(() => {
        this.toggleModal(false);
      }, 3000);
    },


    toggleModal(show) {
      this.isModalVisible = show;
    },

    clearCodeMirror() {
      this.codeContent = ""; // Set the content to an empty string to clear it
    },

    executeCode() {
      // Check if any tags are selected
      const selectedTags = document.querySelectorAll('.tagCheckbox:checked');
      if (selectedTags.length === 0) {
        // No tags selected, show Bootstrap alert modal
        $('#tagAlertModal').modal('show');
      } else {
        // Tags are selected, proceed with running the code
        // Add your code execution logic here
      }
    },

    async submitRepoURL() {
      this.isSubmitting = true;
      try {
        let response = await this.$axios.post("/your-api-endpoint", {
          url: this.githubRepoURL,
        });

        if (response.data.success) {
          this.submissionMessage = "URL successfully submitted!";
        } else {
          this.submissionMessage = "Failed to submit URL. Please try again.";
        }
      } catch (error) {
        this.submissionMessage = "URL successfully submitted !";
      } finally {
        this.isSubmitting = false;

        // Automatically hide the message after 3 seconds
        setTimeout(() => {
          this.submissionMessage = ''; // Clear the message
        }, 3000);
      }
    }
  }
};
</script>

<style scoped>
#cy {
  height: 200px;
  width: 100%;
  margin: 0 auto;
  display: block;
}

/* Directly target the content of CodeMirror */
.CodeMirror {
  color: rgb(13, 13, 206);
  /* This will change the default text color */
}

.cm-keyword {
  color: rgb(55, 20, 226);
  /* This will change the color of keywords */
}


.cm-string {
  color: rgb(4, 100, 4);
  /* This will change the color of strings */
}
</style>

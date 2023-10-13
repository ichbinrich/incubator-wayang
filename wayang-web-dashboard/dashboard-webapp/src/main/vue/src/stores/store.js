// In your Pinia store
import { defineStore } from 'pinia';

export const useMyStore = defineStore('myStore', {
  state: () => ({
    debuggableTuples: [], // Store debuggable tuples here
  }),
  actions: {
    addDebuggableTuple(tuple) {
      this.debuggableTuples.push(tuple);
    },
    // Implement other actions as needed
  },
});

import ApiClient from '../services/ApiClient.js';
import Autocomplete from './Autocomplete.js';

export default {
    components: {
        Autocomplete
    },
    template: `
        <div class="space-y-6">
            <!-- Selection Controls -->
            <div class="bg-white rounded-lg shadow p-4 md:p-6">
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 md:gap-4">
                    <!-- Domain Selector -->
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Domain</label>
                        <Autocomplete 
                            v-model="selectedDomain"
                            :items="domains"
                            :disabled="loadingMetadata"
                            :loading="loadingMetadata"
                            placeholder="Type to search..."
                            label="Domain"
                            @select="onDomainChange"
                        />
                    </div>
                    
                    <!-- Application Selector -->
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Application</label>
                        <Autocomplete 
                            v-model="selectedApplication"
                            :items="applicationsForDomain"
                            :disabled="!selectedDomain || loadingMetadata"
                            :loading="loadingMetadata"
                            placeholder="Type to search..."
                            label="Application"
                        />
                    </div>
                    
                    <!-- Load Button -->
                    <div class="flex items-end col-span-1 sm:col-span-2 lg:col-span-2">
                        <button 
                            @click="loadProperties"
                            :disabled="loading || !selectedDomain || !selectedApplication"
                            class="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 font-medium transition-colors"
                        >
                            {{ loading ? 'Loading...' : 'Load Properties' }}
                        </button>
                    </div>
                </div>
                
                <!-- Error Alert -->
                <div v-if="error" class="mt-4 p-4 bg-red-50 border border-red-200 rounded-md">
                    <p class="text-sm text-red-600">{{ error }}</p>
                </div>
            </div>
            
            <!-- Version Info -->
            <div v-if="syncInfo" class="bg-blue-50 border border-blue-200 rounded-lg p-4 md:p-6">
                <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                    <div>
                        <p class="text-xs text-blue-600 font-medium">Version</p>
                        <p class="text-lg font-bold text-blue-900">{{ syncInfo.versionNumber }}</p>
                    </div>
                    <div>
                        <p class="text-xs text-blue-600 font-medium">Updated By</p>
                        <p class="text-sm text-blue-900">{{ syncInfo.updatedBy }}</p>
                    </div>
                    <div>
                        <p class="text-xs text-blue-600 font-medium">Updated At</p>
                        <p class="text-sm text-blue-900">{{ formatDate(syncInfo.updatedTm) }}</p>
                    </div>
                </div>
            </div>
            
            <!-- Properties Table - Responsive -->
            <div v-if="properties.length > 0" class="bg-white rounded-lg shadow overflow-x-auto">
                <!-- Desktop Table -->
                <table class="hidden md:table w-full">
                    <thead class="bg-gray-100 border-b">
                        <tr>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Property Key</th>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Value</th>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Updated By</th>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Updated</th>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Actions</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y">
                        <tr v-for="prop in properties" :key="prop.propertyKey" class="hover:bg-gray-50">
                            <td class="px-4 md:px-6 py-4 text-sm font-medium text-gray-900">{{ prop.propertyKey }}</td>
                            <td class="px-4 md:px-6 py-4 text-sm text-gray-600">
                                <input 
                                    v-model="editedProperties[prop.propertyKey]"
                                    type="text"
                                    class="w-full px-2 py-1 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500"
                                />
                            </td>
                            <td class="px-4 md:px-6 py-4 text-sm text-gray-600">{{ prop.updatedBy }}</td>
                            <td class="px-4 md:px-6 py-4 text-sm text-gray-600">{{ formatDate(prop.updatedTm) }}</td>
                            <td class="px-4 md:px-6 py-4 text-sm space-x-2">
                                <button 
                                    @click="deleteProperty(prop.propertyKey)"
                                    class="text-red-600 hover:text-red-900 font-medium"
                                >
                                    Delete
                                </button>
                            </td>
                        </tr>
                    </tbody>
                </table>

                <!-- Mobile Card View -->
                <div class="md:hidden divide-y">
                    <div v-for="prop in properties" :key="prop.propertyKey" class="p-4 border-b hover:bg-gray-50">
                        <div class="mb-3">
                            <p class="text-xs font-medium text-gray-500 uppercase">Property Key</p>
                            <p class="font-semibold text-gray-900">{{ prop.propertyKey }}</p>
                        </div>
                        <div class="mb-3">
                            <p class="text-xs font-medium text-gray-500 uppercase">Value</p>
                            <input 
                                v-model="editedProperties[prop.propertyKey]"
                                type="text"
                                class="w-full px-2 py-1 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 text-sm"
                            />
                        </div>
                        <div class="grid grid-cols-2 gap-2 mb-3">
                            <div>
                                <p class="text-xs font-medium text-gray-500 uppercase">Updated By</p>
                                <p class="text-sm text-gray-600">{{ prop.updatedBy }}</p>
                            </div>
                            <div>
                                <p class="text-xs font-medium text-gray-500 uppercase">Updated</p>
                                <p class="text-sm text-gray-600">{{ formatDate(prop.updatedTm) }}</p>
                            </div>
                        </div>
                        <button 
                            @click="deleteProperty(prop.propertyKey)"
                            class="w-full text-red-600 hover:text-red-900 font-medium text-sm py-2"
                        >
                            Delete
                        </button>
                    </div>
                </div>
                
                <!-- Update Button -->
                <div class="bg-gray-50 px-4 md:px-6 py-4 border-t flex flex-col sm:flex-row gap-3 justify-end">
                    <button 
                        @click="resetEdits"
                        class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 font-medium w-full sm:w-auto"
                    >
                        Reset
                    </button>
                    <button 
                        @click="saveChanges"
                        :disabled="updateLoading"
                        class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:bg-gray-400 font-medium w-full sm:w-auto"
                    >
                        {{ updateLoading ? 'Saving...' : 'Publish Changes' }}
                    </button>
                </div>
            </div>
            
            <!-- Empty State -->
            <div v-if="!loading && properties.length === 0 && syncInfo" class="bg-gray-50 rounded-lg p-6 md:p-8 text-center">
                <p class="text-gray-500">No properties found. Use the form below to add one.</p>
            </div>
            
            <!-- Add New Property -->
            <div v-if="syncInfo" class="bg-white rounded-lg shadow p-4 md:p-6">
                <h3 class="text-lg font-semibold text-gray-900 mb-4">Add New Property</h3>
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 md:gap-4">
                    <input 
                        v-model="newProperty.key"
                        type="text"
                        placeholder="Property Key"
                        class="px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500"
                    />
                    <input 
                        v-model="newProperty.value"
                        type="text"
                        placeholder="Property Value"
                        class="px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500"
                    />
                    <button 
                        @click="addProperty"
                        :disabled="!newProperty.key || !newProperty.value || addLoading"
                        class="px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 disabled:bg-gray-400 font-medium w-full"
                    >
                        {{ addLoading ? 'Adding...' : 'Add Property' }}
                    </button>
                </div>
                <div v-if="addError" class="mt-4 p-4 bg-red-50 border border-red-200 rounded-md">
                    <p class="text-sm text-red-600">{{ addError }}</p>
                </div>
            </div>
        </div>
    `,
    data() {
        return {
            selectedDomain: '',
            selectedApplication: '',
            domains: [],
            applicationsByDomain: {},
            properties: [],
            editedProperties: {},
            syncInfo: null,
            loading: false,
            loadingMetadata: false,
            updateLoading: false,
            addLoading: false,
            error: null,
            addError: null,
            newProperty: {
                key: '',
                value: ''
            }
        };
    },
    computed: {
        applicationsForDomain() {
            if (!this.selectedDomain || !this.applicationsByDomain[this.selectedDomain]) {
                return [];
            }
            return this.applicationsByDomain[this.selectedDomain];
        }
    },
    mounted() {
        this.fetchMetadata();

        // Listen for service-onboarded event from ServiceOnboard component
        window.addEventListener('service-onboarded', this.handleServiceOnboarded);
    },
    beforeUnmount() {
        // Clean up event listener
        window.removeEventListener('service-onboarded', this.handleServiceOnboarded);
    },
    methods: {
        handleServiceOnboarded(event) {
            // Auto-populate domain and application from the event
            const { domain, application } = event.detail;
            this.selectedDomain = domain;
            this.selectedApplication = application;

            // Auto-load the properties for the newly onboarded service
            this.$nextTick(() => {
                this.loadProperties();
            });
        },

        async fetchMetadata() {
            this.loadingMetadata = true;
            this.error = null;

            try {
                const metadata = await ApiClient.getMetadata();
                this.domains = metadata.domains || [];
                this.applicationsByDomain = metadata.applicationsByDomain || {};
            } catch (err) {
                this.error = 'Failed to load domains and applications: ' + (err.message || 'Unknown error');
            } finally {
                this.loadingMetadata = false;
            }
        },

        onDomainChange() {
            // Reset application selection when domain changes
            this.selectedApplication = '';
            this.properties = [];
            this.syncInfo = null;
            this.editedProperties = {};
            this.error = null;
        },
        async loadProperties() {
            if (!this.selectedDomain || !this.selectedApplication) {
                this.error = 'Please enter both domain and application';
                return;
            }

            this.loading = true;
            this.error = null;

            try {
                const response = await ApiClient.getProperties(this.selectedDomain, this.selectedApplication);
                this.properties = response.properties;
                this.syncInfo = response.syncInfo;
                this.editedProperties = {};

                // Initialize editedProperties with current values
                this.properties.forEach(prop => {
                    this.editedProperties[prop.propertyKey] = prop.propertyValue;
                });
            } catch (err) {
                this.error = err.message || 'Failed to load properties';
            } finally {
                this.loading = false;
            }
        },

        async saveChanges() {
            this.updateLoading = true;
            this.error = null;

            try {
                // Build update request
                const updatedProps = {};
                this.properties.forEach(prop => {
                    if (this.editedProperties[prop.propertyKey] !== prop.propertyValue) {
                        updatedProps[prop.propertyKey] = this.editedProperties[prop.propertyKey];
                    }
                });

                if (Object.keys(updatedProps).length === 0) {
                    this.error = 'No changes to save';
                    return;
                }

                const response = await ApiClient.updateProperties(
                    this.selectedDomain,
                    this.selectedApplication,
                    updatedProps,
                    this.syncInfo.versionNumber,
                    this.syncInfo.updatedTm
                );

                this.properties = response.properties;
                this.syncInfo = response.syncInfo;
                this.editedProperties = {};

                // Re-initialize edited properties
                this.properties.forEach(prop => {
                    this.editedProperties[prop.propertyKey] = prop.propertyValue;
                });
            } catch (err) {
                this.error = err.message || 'Failed to update properties';
            } finally {
                this.updateLoading = false;
            }
        },

        async addProperty() {
            this.addLoading = true;
            this.addError = null;

            try {
                const response = await ApiClient.addProperty(
                    this.selectedDomain,
                    this.selectedApplication,
                    this.newProperty.key,
                    this.newProperty.value
                );

                // Reload properties
                await this.loadProperties();
                this.newProperty = { key: '', value: '' };
            } catch (err) {
                this.addError = err.message || 'Failed to add property';
            } finally {
                this.addLoading = false;
            }
        },

        async deleteProperty(key) {
            if (!confirm(`Are you sure you want to delete ${key}?`)) {
                return;
            }

            this.updateLoading = true;
            this.error = null;

            try {
                const response = await ApiClient.deleteProperty(
                    this.selectedDomain,
                    this.selectedApplication,
                    key
                );

                // Update properties and sync info from response
                this.properties = response.properties;
                this.syncInfo = response.syncInfo;
                this.editedProperties = {};

                // Re-initialize edited properties
                this.properties.forEach(prop => {
                    this.editedProperties[prop.propertyKey] = prop.propertyValue;
                });
            } catch (err) {
                this.error = err.message || 'Failed to delete property';
            } finally {
                this.updateLoading = false;
            }
        },

        resetEdits() {
            this.properties.forEach(prop => {
                this.editedProperties[prop.propertyKey] = prop.propertyValue;
            });
        },

        formatDate(dateString) {
            const date = new Date(dateString);
            return date.toLocaleString();
        }
    }
};


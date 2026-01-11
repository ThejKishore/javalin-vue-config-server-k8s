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
                            @click="loadAuditHistory"
                            :disabled="loading || !selectedDomain || !selectedApplication"
                            class="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 font-medium transition-colors"
                        >
                            {{ loading ? 'Loading...' : 'Load History' }}
                        </button>
                    </div>
                </div>
                
                <!-- Error Alert -->
                <div v-if="error" class="mt-4 p-4 bg-red-50 border border-red-200 rounded-md">
                    <p class="text-sm text-red-600">{{ error }}</p>
                </div>
            </div>
            
            <!-- Audit History Table - Responsive -->
            <div v-if="auditHistory.length > 0" class="bg-white rounded-lg shadow overflow-x-auto">
                <!-- Desktop Table -->
                <table class="hidden md:table w-full">
                    <thead class="bg-gray-100 border-b">
                        <tr>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Version</th>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Property Key</th>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Operation</th>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Old Value</th>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">New Value</th>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Updated By</th>
                            <th class="px-4 md:px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Updated At</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y">
                        <tr v-for="audit in auditHistory" :key="audit.id" class="hover:bg-gray-50">
                            <td class="px-4 md:px-6 py-4 text-sm font-bold text-blue-600">{{ audit.versionNumber }}</td>
                            <td class="px-4 md:px-6 py-4 text-sm font-medium text-gray-900">{{ audit.propertyKey }}</td>
                            <td class="px-4 md:px-6 py-4 text-sm">
                                <span :class="getOperationBadgeClass(audit.operation)">
                                    {{ audit.operation }}
                                </span>
                            </td>
                            <td class="px-4 md:px-6 py-4 text-sm text-gray-600">
                                <code class="bg-gray-100 px-2 py-1 rounded text-xs">{{ audit.oldPropertyValue || '-' }}</code>
                            </td>
                            <td class="px-4 md:px-6 py-4 text-sm text-gray-600">
                                <code class="bg-gray-100 px-2 py-1 rounded text-xs">{{ audit.newPropertyValue || '-' }}</code>
                            </td>
                            <td class="px-4 md:px-6 py-4 text-sm text-gray-600">{{ audit.updatedBy }}</td>
                            <td class="px-4 md:px-6 py-4 text-sm text-gray-600">{{ formatDate(audit.updatedTm) }}</td>
                        </tr>
                    </tbody>
                </table>

                <!-- Mobile Card View -->
                <div class="md:hidden divide-y">
                    <div v-for="audit in auditHistory" :key="audit.id" class="p-4 border-b hover:bg-gray-50">
                        <div class="flex justify-between items-start mb-3">
                            <div>
                                <p class="text-xs font-medium text-gray-500 uppercase">Version</p>
                                <p class="font-bold text-blue-600">{{ audit.versionNumber }}</p>
                            </div>
                            <span :class="getOperationBadgeClass(audit.operation)">
                                {{ audit.operation }}
                            </span>
                        </div>
                        <div class="mb-3">
                            <p class="text-xs font-medium text-gray-500 uppercase">Property Key</p>
                            <p class="font-semibold text-gray-900">{{ audit.propertyKey }}</p>
                        </div>
                        <div class="grid grid-cols-2 gap-2 mb-3">
                            <div>
                                <p class="text-xs font-medium text-gray-500 uppercase">Old Value</p>
                                <code class="bg-gray-100 px-2 py-1 rounded text-xs block">{{ audit.oldPropertyValue || '-' }}</code>
                            </div>
                            <div>
                                <p class="text-xs font-medium text-gray-500 uppercase">New Value</p>
                                <code class="bg-gray-100 px-2 py-1 rounded text-xs block">{{ audit.newPropertyValue || '-' }}</code>
                            </div>
                        </div>
                        <div class="grid grid-cols-2 gap-2">
                            <div>
                                <p class="text-xs font-medium text-gray-500 uppercase">Updated By</p>
                                <p class="text-sm text-gray-600">{{ audit.updatedBy }}</p>
                            </div>
                            <div>
                                <p class="text-xs font-medium text-gray-500 uppercase">Updated At</p>
                                <p class="text-sm text-gray-600">{{ formatDate(audit.updatedTm) }}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Empty State -->
            <div v-if="!loading && auditHistory.length === 0 && attemptedLoad" class="bg-gray-50 rounded-lg p-6 md:p-8 text-center">
                <p class="text-gray-500">No audit history found for the selected application and domain.</p>
            </div>
            
            <!-- Initial State -->
            <div v-if="!attemptedLoad" class="bg-gray-50 rounded-lg p-6 md:p-8 text-center">
                <p class="text-gray-500">Select a domain and application to view audit history.</p>
            </div>
        </div>
    `,
    data() {
        return {
            selectedDomain: '',
            selectedApplication: '',
            domains: [],
            applicationsByDomain: {},
            auditHistory: [],
            loading: false,
            loadingMetadata: false,
            error: null,
            attemptedLoad: false
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
    },
    methods: {
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
            // Reset application selection and history when domain changes
            this.selectedApplication = '';
            this.auditHistory = [];
            this.attemptedLoad = false;
            this.error = null;
        },

        async loadAuditHistory() {
            if (!this.selectedDomain || !this.selectedApplication) {
                this.error = 'Please enter both domain and application';
                return;
            }

            this.loading = true;
            this.error = null;
            this.attemptedLoad = true;

            try {
                this.auditHistory = await ApiClient.getAuditHistory(
                    this.selectedDomain,
                    this.selectedApplication
                );
            } catch (err) {
                this.error = err.message || 'Failed to load audit history';
            } finally {
                this.loading = false;
            }
        },

        getOperationBadgeClass(operation) {
            const baseClass = 'px-3 py-1 rounded-full text-xs font-medium';
            switch (operation) {
                case 'ADDED':
                    return `${baseClass} bg-green-100 text-green-800`;
                case 'MODIFIED':
                    return `${baseClass} bg-blue-100 text-blue-800`;
                case 'DELETED':
                    return `${baseClass} bg-red-100 text-red-800`;
                default:
                    return `${baseClass} bg-gray-100 text-gray-800`;
            }
        },

        formatDate(dateString) {
            const date = new Date(dateString);
            return date.toLocaleString();
        }
    }
};


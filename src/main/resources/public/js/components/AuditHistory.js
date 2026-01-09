import ApiClient from '../services/ApiClient.js';

export default {
    template: `
        <div class="space-y-6">
            <!-- Selection Controls -->
            <div class="bg-white rounded-lg shadow p-6">
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <!-- Domain Selector -->
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Domain</label>
                        <select 
                            v-model="selectedDomain"
                            @change="onDomainChange"
                            :disabled="loadingMetadata"
                            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        >
                            <option value="">{{ loadingMetadata ? 'Loading domains...' : 'Select a domain' }}</option>
                            <option v-for="domain in domains" :key="domain" :value="domain">
                                {{ domain }}
                            </option>
                        </select>
                    </div>
                    
                    <!-- Application Selector -->
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Application</label>
                        <select 
                            v-model="selectedApplication"
                            :disabled="!selectedDomain || loadingMetadata"
                            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        >
                            <option value="">{{ !selectedDomain ? 'Select a domain first' : 'Select an application' }}</option>
                            <option v-for="app in applicationsForDomain" :key="app" :value="app">
                                {{ app }}
                            </option>
                        </select>
                    </div>
                    
                    <!-- Load Button -->
                    <div class="flex items-end">
                        <button 
                            @click="loadAuditHistory"
                            :disabled="loading || !selectedDomain || !selectedApplication"
                            class="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 font-medium"
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
            
            <!-- Audit History Table -->
            <div v-if="auditHistory.length > 0" class="bg-white rounded-lg shadow overflow-hidden">
                <table class="w-full">
                    <thead class="bg-gray-100 border-b">
                        <tr>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Version</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Property Key</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Operation</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Old Value</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">New Value</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Updated By</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-700 uppercase">Updated At</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y">
                        <tr v-for="audit in auditHistory" :key="audit.id" class="hover:bg-gray-50">
                            <td class="px-6 py-4 text-sm font-bold text-blue-600">{{ audit.versionNumber }}</td>
                            <td class="px-6 py-4 text-sm font-medium text-gray-900">{{ audit.propertyKey }}</td>
                            <td class="px-6 py-4 text-sm">
                                <span :class="getOperationBadgeClass(audit.operation)">
                                    {{ audit.operation }}
                                </span>
                            </td>
                            <td class="px-6 py-4 text-sm text-gray-600">
                                <code class="bg-gray-100 px-2 py-1 rounded">{{ audit.oldPropertyValue || '-' }}</code>
                            </td>
                            <td class="px-6 py-4 text-sm text-gray-600">
                                <code class="bg-gray-100 px-2 py-1 rounded">{{ audit.newPropertyValue || '-' }}</code>
                            </td>
                            <td class="px-6 py-4 text-sm text-gray-600">{{ audit.updatedBy }}</td>
                            <td class="px-6 py-4 text-sm text-gray-600">{{ formatDate(audit.updatedTm) }}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            
            <!-- Empty State -->
            <div v-if="!loading && auditHistory.length === 0 && attemptedLoad" class="bg-gray-50 rounded-lg p-8 text-center">
                <p class="text-gray-500">No audit history found for the selected application and domain.</p>
            </div>
            
            <!-- Initial State -->
            <div v-if="!attemptedLoad" class="bg-gray-50 rounded-lg p-8 text-center">
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


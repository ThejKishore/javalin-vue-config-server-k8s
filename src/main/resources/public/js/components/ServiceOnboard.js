import ApiClient from '../services/ApiClient.js';

export default {
    template: `
        <div class="space-y-6">
            <!-- Header -->
            <div class="bg-white rounded-lg shadow p-4 md:p-6">
                <h2 class="text-2xl font-bold text-gray-900 mb-2">Service Onboarding</h2>
                <p class="text-gray-600">Onboard a new service by providing domain and application details along with configuration file</p>
            </div>

            <!-- Onboarding Form -->
            <div class="bg-white rounded-lg shadow p-4 md:p-6">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6 mb-6">
                    <!-- Domain Input -->
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Domain *</label>
                        <input 
                            v-model="formData.domain"
                            type="text"
                            placeholder="e.g., production, staging, development"
                            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            :disabled="uploading"
                        />
                        <p class="text-xs text-gray-500 mt-1">Domain where the service will be configured</p>
                    </div>

                    <!-- Application Input -->
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Application Name *</label>
                        <input 
                            v-model="formData.application"
                            type="text"
                            placeholder="e.g., user-service, api-gateway"
                            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            :disabled="uploading"
                        />
                        <p class="text-xs text-gray-500 mt-1">Unique application identifier</p>
                    </div>
                </div>

                <!-- File Upload -->
                <div class="mb-6">
                    <label class="block text-sm font-medium text-gray-700 mb-2">Configuration File *</label>
                    <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-500 transition-colors cursor-pointer"
                         @click="$refs.fileInput.click()"
                         @drop="handleFileDrop"
                         @dragover.prevent="isDragging = true"
                         @dragleave="isDragging = false"
                         :class="isDragging ? 'border-blue-500 bg-blue-50' : ''">
                        <input 
                            ref="fileInput"
                            type="file"
                            accept=".properties,.yaml,.yml,.json"
                            @change="handleFileSelect"
                            class="hidden"
                            :disabled="uploading"
                        />
                        <div v-if="!formData.file" class="space-y-2">
                            <p class="text-gray-600">📁 Drop your file here or click to browse</p>
                            <p class="text-xs text-gray-500">Supported formats: .properties, .yaml, .yml, .json</p>
                        </div>
                        <div v-else class="space-y-2">
                            <p class="text-green-600 font-medium">✓ {{ formData.file.name }}</p>
                            <p class="text-xs text-gray-500">{{ (formData.file.size / 1024).toFixed(2) }} KB</p>
                            <button 
                                @click.stop="clearFile"
                                class="text-sm text-red-600 hover:text-red-700 mt-2"
                                :disabled="uploading"
                            >
                                Remove file
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Error Alert -->
                <div v-if="error" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-md">
                    <p class="text-sm text-red-600"><strong>Error:</strong> {{ error }}</p>
                </div>

                <!-- Success Alert -->
                <div v-if="successMessage" class="mb-6 p-4 bg-green-50 border border-green-200 rounded-md">
                    <p class="text-sm text-green-600"><strong>Success:</strong> {{ successMessage }}</p>
                </div>

                <!-- Buttons -->
                <div class="flex flex-col sm:flex-row gap-3 justify-end">
                    <button 
                        @click="resetForm"
                        class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 font-medium transition-colors"
                        :disabled="uploading"
                    >
                        Reset
                    </button>
                    <button 
                        @click="uploadService"
                        :disabled="uploading || !isFormValid"
                        class="px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:bg-gray-400 font-medium transition-colors flex items-center justify-center gap-2"
                    >
                        <span v-if="uploading" class="animate-spin">⟳</span>
                        {{ uploading ? 'Uploading...' : 'Onboard Service' }}
                    </button>
                </div>
            </div>

            <!-- Info Box -->
            <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 md:p-6">
                <h3 class="font-semibold text-blue-900 mb-3">What happens during onboarding?</h3>
                <ul class="space-y-2 text-sm text-blue-800">
                    <li>✓ Your configuration file is validated</li>
                    <li>✓ System checks for duplicate domain + application combinations</li>
                    <li>✓ Configuration is stored securely in the database</li>
                    <li>✓ Service becomes available in Configuration Management</li>
                    <li>✓ You can immediately manage properties via the Config Management screen</li>
                </ul>
            </div>
        </div>
    `,
    data() {
        return {
            formData: {
                domain: '',
                application: '',
                file: null
            },
            error: '',
            successMessage: '',
            uploading: false,
            isDragging: false
        };
    },
    computed: {
        isFormValid() {
            return this.formData.domain.trim() !== '' &&
                   this.formData.application.trim() !== '' &&
                   this.formData.file !== null;
        }
    },
    methods: {
        handleFileSelect(event) {
            const file = event.target.files[0];
            if (file) {
                this.formData.file = file;
                this.error = '';
            }
        },
        handleFileDrop(event) {
            event.preventDefault();
            this.isDragging = false;
            const file = event.dataTransfer.files[0];
            if (file) {
                this.formData.file = file;
                this.error = '';
            }
        },
        clearFile() {
            this.formData.file = null;
            this.$refs.fileInput.value = '';
        },
        resetForm() {
            this.formData = {
                domain: '',
                application: '',
                file: null
            };
            this.error = '';
            this.successMessage = '';
            if (this.$refs.fileInput) {
                this.$refs.fileInput.value = '';
            }
        },
        async uploadService() {
            this.uploading = true;
            this.error = '';
            this.successMessage = '';

            try {
                // Validate inputs
                if (!this.formData.domain.trim()) {
                    throw new Error('Domain is required');
                }
                if (!this.formData.application.trim()) {
                    throw new Error('Application name is required');
                }
                if (!this.formData.file) {
                    throw new Error('Configuration file is required');
                }

                // Call backend API
                const result = await ApiClient.onboardService(
                    this.formData.domain,
                    this.formData.application,
                    this.formData.file
                );

                this.successMessage = `Service '${this.formData.application}' successfully onboarded in domain '${this.formData.domain}'`;

                // Reset form after 2 seconds and navigate to config management
                setTimeout(() => {
                    this.resetForm();
                    // Emit event to parent to navigate to config management with the new service
                    this.$emit('service-onboarded', {
                        domain: this.formData.domain,
                        application: this.formData.application
                    });
                }, 2000);

            } catch (err) {
                this.error = err.message || 'Failed to onboard service. Please try again.';
                console.error('Onboarding error:', err);
            } finally {
                this.uploading = false;
            }
        }
    }
};


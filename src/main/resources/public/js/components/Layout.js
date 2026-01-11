import Sidebar from './Sidebar.js';
import ConfigManagement from './ConfigManagement.js';
import AuditHistory from './AuditHistory.js';
import ServiceOnboard from './ServiceOnboard.js';

export default {
    template: `
        <div class="flex h-screen bg-gray-50 flex-col md:flex-row">
            <!-- Mobile Header -->
            <div class="md:hidden bg-white shadow-md p-4 flex justify-between items-center">
                <h1 class="text-lg font-bold text-gray-900">Platform Management</h1>
                <button 
                    @click="sidebarOpen = !sidebarOpen"
                    class="p-2 hover:bg-gray-100 rounded-md"
                >
                    <span class="text-2xl">{{ sidebarOpen ? '✕' : '☰' }}</span>
                </button>
            </div>

            <!-- Sidebar -->
            <Sidebar 
                @navigate="handleNavigation"
                :isMobile="isMobile"
                :isOpen="sidebarOpen"
                @close="sidebarOpen = false"
            />
            
            <!-- Main Content -->
            <div class="flex-1 flex flex-col overflow-hidden">
                <!-- Desktop Header -->
                <header class="hidden md:block bg-white shadow">
                    <div class="px-4 md:px-8 py-4">
                        <h1 class="text-2xl font-bold text-gray-900">{{ getPageTitle() }}</h1>
                    </div>
                </header>
                
                <!-- Content -->
                <main class="flex-1 overflow-auto p-4 md:p-8">
                    <ConfigManagement v-if="currentView === 'config'" />
                    <AuditHistory v-if="currentView === 'audit'" />
                    <ServiceOnboard 
                        v-if="currentView === 'onboard'"
                        @service-onboarded="handleServiceOnboarded"
                    />
                </main>
            </div>

            <!-- Mobile Overlay -->
            <div 
                v-if="sidebarOpen && isMobile"
                class="fixed inset-0 bg-black bg-opacity-50 z-30 md:hidden"
                @click="sidebarOpen = false"
            ></div>
        </div>
    `,
    components: {
        Sidebar,
        ConfigManagement,
        AuditHistory,
        ServiceOnboard
    },
    data() {
        return {
            currentView: 'config',
            sidebarOpen: false,
            isMobile: window.innerWidth < 768
        };
    },
    mounted() {
        window.addEventListener('resize', this.handleResize);
    },
    beforeUnmount() {
        window.removeEventListener('resize', this.handleResize);
    },
    methods: {
        handleNavigation(view) {
            this.currentView = view;
            if (this.isMobile) {
                this.sidebarOpen = false;
            }
        },
        handleResize() {
            this.isMobile = window.innerWidth < 768;
            if (!this.isMobile) {
                this.sidebarOpen = false;
            }
        },
        getPageTitle() {
            const titles = {
                'config': 'Configuration Management',
                'audit': 'Audit History',
                'onboard': 'Service Onboarding'
            };
            return titles[this.currentView] || 'Platform Management';
        },
        handleServiceOnboarded(serviceData) {
            // Store the newly onboarded service data
            this.newServiceData = serviceData;

            // Navigate to config management to show the loaded data
            this.currentView = 'config';

            // Emit event to ConfigManagement to load the new service
            // This will be handled by ConfigManagement component
            this.$nextTick(() => {
                window.dispatchEvent(new CustomEvent('service-onboarded', {
                    detail: serviceData
                }));
            });
        }
    }
};


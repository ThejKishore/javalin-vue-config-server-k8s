export default {
    template: `
        <aside :class="['bg-gray-900 text-white shadow-lg transition-all duration-300 ease-in-out', 
                        isMobile ? (isOpen ? 'fixed inset-y-16 left-0 w-64 z-40' : 'hidden') : 'w-64 relative']">
            <!-- Logo -->
            <div class="p-6 border-b border-gray-700">
                <h2 class="text-xl font-bold">Platform Config</h2>
                <p class="text-xs text-gray-400 mt-1">Configuration Management</p>
            </div>
            
            <!-- Navigation Menu -->
            <nav class="mt-6 pb-24 md:pb-6">
                <!-- Configuration Management Parent Menu -->
                <div>
                    <!-- Parent Menu Button -->
                    <button 
                        @click="toggleMenu('configMgmt')"
                        :class="['w-full text-left px-6 py-3 flex items-center justify-between transition-colors', 
                                 menuExpanded.configMgmt ? 'bg-gray-800' : 'text-gray-300 hover:bg-gray-800']"
                    >
                        <span class="flex items-center space-x-3">
                            <span class="text-xl">📊</span>
                            <span class="font-medium">Configuration Management</span>
                        </span>
                        <span class="transition-transform" :style="{ transform: menuExpanded.configMgmt ? 'rotate(180deg)' : 'rotate(0)' }">
                            ▼
                        </span>
                    </button>
                    
                    <!-- Submenu Items -->
                    <div v-show="menuExpanded.configMgmt" class="bg-gray-800 space-y-0">
                        <!-- Configuration Submenu -->
                        <button 
                            @click="navigate('config')"
                            :class="['w-full text-left px-8 py-2.5 flex items-center space-x-3 transition-colors text-sm', 
                                     currentView === 'config' ? 'bg-blue-600 text-white' : 'text-gray-300 hover:bg-gray-700']"
                        >
                            <span class="text-lg">⚙️</span>
                            <span>Configuration</span>
                        </button>
                        
                        <!-- Audit History Submenu -->
                        <button 
                            @click="navigate('audit')"
                            :class="['w-full text-left px-8 py-2.5 flex items-center space-x-3 transition-colors text-sm', 
                                     currentView === 'audit' ? 'bg-blue-600 text-white' : 'text-gray-300 hover:bg-gray-700']"
                        >
                            <span class="text-lg">📋</span>
                            <span>Audit History</span>
                        </button>
                        
                        <!-- Service Onboard Submenu -->
                        <button 
                            @click="navigate('onboard')"
                            :class="['w-full text-left px-8 py-2.5 flex items-center space-x-3 transition-colors text-sm', 
                                     currentView === 'onboard' ? 'bg-blue-600 text-white' : 'text-gray-300 hover:bg-gray-700']"
                        >
                            <span class="text-lg">🚀</span>
                            <span>Service Onboard</span>
                        </button>
                    </div>
                </div>
            </nav>
            
            <!-- Footer -->
            <div :class="['p-6 border-t border-gray-700', isMobile ? 'absolute bottom-0 w-64' : 'md:absolute md:bottom-0 md:w-64']">
                <p class="text-xs text-gray-500">Version 1.0.0</p>
            </div>
        </aside>
    `,
    props: {
        isMobile: Boolean,
        isOpen: Boolean
    },
    data() {
        return {
            currentView: 'config',
            menuExpanded: {
                configMgmt: true
            }
        };
    },
    methods: {
        toggleMenu(menuName) {
            this.menuExpanded[menuName] = !this.menuExpanded[menuName];
        },
        navigate(view) {
            this.currentView = view;
            this.$emit('navigate', view);
            this.$emit('close');
        }
    }
};


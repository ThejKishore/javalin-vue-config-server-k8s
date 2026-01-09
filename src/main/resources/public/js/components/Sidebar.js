export default {
    template: `
        <aside class="w-64 bg-gray-900 text-white shadow-lg">
            <!-- Logo -->
            <div class="p-6 border-b border-gray-700">
                <h2 class="text-xl font-bold">Platform Config</h2>
                <p class="text-xs text-gray-400 mt-1">Configuration Management</p>
            </div>
            
            <!-- Navigation Menu -->
            <nav class="mt-6">
                <button 
                    @click="navigate('config')"
                    :class="['w-full text-left px-6 py-3 flex items-center space-x-3', 
                             currentView === 'config' ? 'bg-blue-600 text-white' : 'text-gray-300 hover:bg-gray-800']"
                >
                    <span class="text-xl">⚙️</span>
                    <span class="font-medium">Configuration</span>
                </button>
                
                <button 
                    @click="navigate('audit')"
                    :class="['w-full text-left px-6 py-3 flex items-center space-x-3', 
                             currentView === 'audit' ? 'bg-blue-600 text-white' : 'text-gray-300 hover:bg-gray-800']"
                >
                    <span class="text-xl">📋</span>
                    <span class="font-medium">Audit History</span>
                </button>
            </nav>
            
            <!-- Footer -->
            <div class="absolute bottom-0 w-64 p-6 border-t border-gray-700">
                <p class="text-xs text-gray-500">Version 1.0.0</p>
            </div>
        </aside>
    `,
    data() {
        return {
            currentView: 'config'
        };
    },
    methods: {
        navigate(view) {
            this.currentView = view;
            this.$emit('navigate', view);
        }
    }
};


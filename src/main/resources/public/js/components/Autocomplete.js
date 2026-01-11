/**
 * Reusable Autocomplete Component with better UX
 */
export default {
    template: `
        <div class="relative w-full">
            <div class="relative">
                <input 
                    v-model="inputValue"
                    @input="onInput"
                    @focus="showDropdown = true"
                    @blur="onBlur"
                    @keydown.down.prevent="selectNext"
                    @keydown.up.prevent="selectPrev"
                    @keydown.enter.prevent="selectItem"
                    @keydown.escape="showDropdown = false"
                    :placeholder="placeholder"
                    :disabled="disabled"
                    :aria-label="label"
                    :aria-expanded="showDropdown"
                    type="text"
                    autocomplete="off"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed transition-colors"
                />
                <div v-if="loading" class="absolute right-3 top-2.5">
                    <div class="animate-spin h-5 w-5 text-blue-500">⟳</div>
                </div>
                <div v-if="inputValue && !loading" class="absolute right-3 top-2.5 cursor-pointer hover:text-gray-600" @click="clearInput">
                    ✕
                </div>
            </div>
            
            <!-- Dropdown -->
            <div 
                v-if="showDropdown && filteredItems.length > 0"
                role="listbox"
                class="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-64 overflow-y-auto"
            >
                <div
                    v-for="(item, index) in filteredItems"
                    :key="index"
                    @click="selectItem(item)"
                    :class="['px-3 py-2 cursor-pointer select-none', selectedIndex === index ? 'bg-blue-500 text-white' : 'hover:bg-gray-100']"
                    role="option"
                    :aria-selected="selectedIndex === index"
                >
                    <div class="text-sm">{{ item }}</div>
                </div>
            </div>
            
            <!-- No results -->
            <div 
                v-if="showDropdown && inputValue && filteredItems.length === 0 && !loading"
                class="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg p-3"
            >
                <p class="text-sm text-gray-500 text-center">No results found for "{{ inputValue }}"</p>
            </div>
        </div>
    `,
    props: {
        modelValue: String,
        items: {
            type: Array,
            default: () => []
        },
        placeholder: String,
        label: String,
        disabled: Boolean,
        loading: Boolean,
        minChars: {
            type: Number,
            default: 0
        }
    },
    data() {
        return {
            inputValue: '',
            showDropdown: false,
            selectedIndex: -1
        };
    },
    computed: {
        filteredItems() {
            if (this.minChars > 0 && this.inputValue.length < this.minChars) {
                return [];
            }

            if (!this.inputValue) {
                return this.items || [];
            }

            const searchTerm = this.inputValue.toLowerCase();
            return (this.items || []).filter(item =>
                item.toLowerCase().includes(searchTerm)
            ).slice(0, 10); // Limit to 10 results
        }
    },
    watch: {
        modelValue(newValue) {
            if (newValue !== this.inputValue) {
                this.inputValue = newValue || '';
            }
        },
        items() {
            this.selectedIndex = -1;
        }
    },
    methods: {
        onInput() {
            this.$emit('update:modelValue', this.inputValue);
            this.$emit('input', this.inputValue);
            this.selectedIndex = -1;
            // Show dropdown when there's input
            if (this.inputValue || this.items.length < 10) {
                this.showDropdown = true;
            }
        },

        selectItem(item) {
            const selectedItem = item || this.filteredItems[this.selectedIndex];
            if (selectedItem) {
                this.inputValue = selectedItem;
                this.$emit('update:modelValue', selectedItem);
                this.$emit('select', selectedItem);
                this.showDropdown = false;
                this.selectedIndex = -1;
            }
        },

        onBlur() {
            setTimeout(() => {
                this.showDropdown = false;
            }, 200);
        },

        selectNext() {
            if (this.selectedIndex < this.filteredItems.length - 1) {
                this.selectedIndex++;
            }
        },

        selectPrev() {
            if (this.selectedIndex > 0) {
                this.selectedIndex--;
            }
        },

        clearInput() {
            this.inputValue = '';
            this.$emit('update:modelValue', '');
            this.$emit('input', '');
            this.$emit('select', '');
            this.showDropdown = false;
        }
    }
};


import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from '@/api/axios' // Correct path to global axios instance
import { AVATAR_DECORATIONS } from '@/constants/avatarConfig'
import { POPOVER_DECORATIONS } from '@/constants/popoverConfig'
import { PROFILE_THEMES } from '@/constants/themeConfig'

export const useCustomItemStore = defineStore('customItem', () => {
    // State
    const avatarItems = ref([])
    const popoverItems = ref([])
    const themeItems = ref([])
    const loading = ref(false)

    const itemDefinitions = ref({}) // Dictionary of all items by ID ~ { id: config } to merge into static constants

    // Actions
    const fetchItemDefinitions = async () => {
        try {
            const res = await axios.get('/items/definitions');
            const defs = {};
            res.data.forEach(item => {
                let config = item.config;
                if (typeof config === 'string') {
                    try {
                        config = JSON.parse(config);
                    } catch (e) {
                         console.error('Failed to parse item config', item);
                    }
                }
                
                // Note: We no longer override Cybercity here because we treat it as purely static in fetchUserItems.
                // If backend returns Cybercity, it will be handled (ignored) there or coexist.
                
                defs[item.id] = {
                    id: item.id,
                    name: item.name,
                    itemType: item.itemType,
                    ...config
                };
            });
            itemDefinitions.value = defs;
        } catch (e) {
            console.error('Failed to fetch item definitions', e);
        }
    }

    const fetchUserItems = async () => {
        loading.value = true
        // Ensure definitions are loaded for shared context if needed
        if (Object.keys(itemDefinitions.value).length === 0) {
            await fetchItemDefinitions();
        }

        try {
            // Fetch dynamic items from backend (My Owned Items)
            const [avatars, popovers, themes] = await Promise.all([
                axios.get('/items/my', { params: { type: 'AVATAR' } }),
                axios.get('/items/my', { params: { type: 'POPOVER' } }),
                axios.get('/items/my', { params: { type: 'THEME' } })
            ]);

            // Merge Logic:
            // 1. Static Defaults (always available, local assets)
            // 2. Backend Items (dynamic, S3 assets)
            
            avatarItems.value = formatMyItems(avatars.data, AVATAR_DECORATIONS)
            popoverItems.value = formatMyItems(popovers.data, POPOVER_DECORATIONS)
            themeItems.value = formatMyItems(themes.data, PROFILE_THEMES)

        } catch (error) {
            console.error('Failed to fetch user items', error)
            // Fallback to just static defaults
            avatarItems.value = formatMyItems([], AVATAR_DECORATIONS)
            popoverItems.value = formatMyItems([], POPOVER_DECORATIONS)
            themeItems.value = formatMyItems([], PROFILE_THEMES)
        } finally {
            loading.value = false
        }
    }

    const formatMyItems = (backendItems, staticDefaults) => {
        const result = {};

        // 1. Static Defaults Injection
        // These are local items that are always available and "Owned".
        if (staticDefaults) {
            Object.keys(staticDefaults).forEach(key => {
                const item = staticDefaults[key];
                result[key] = {
                    key: key,      
                    id: key,       // Use key as ID for static items
                    isDefault: true, // Mark as default
                    isOwned: true,   // Always owned
                    ...item
                };
            });
        }

        // 2. Backend Items Merge
        if (backendItems) {
            backendItems.forEach(item => {
                 let config = item.config;
                 if (typeof config === 'string') {
                     try { config = JSON.parse(config); } catch(e){}
                 }
                 
                 // If the backend item has the same ID as a static item (e.g. 'cybercity'), 
                 // we DO NOT overwrite the static item because we want the Local version to persist.
                 // However, we might want to capture the 'isDefault' flag from DB if it tracks user selection?
                 // No, `isDefault` here probably means "Is this the user's currently selected theme?".
                 // Wait, `CustomItemResponse` usually has `isDefault` (meaning System Default) or `selected`?
                 // Checking the DTO in previous turns: `isDefault` usually means "Default Item for everyone".
                 // `isOwned` means "User owns this".
                 
                 // If there is a key collision, we respect the Static definition for the CONFIG/IMAGE,
                 // but we might accept metadata if absolutely necessary. for now, strict separation means we skip if exists.
                 // Actually, let's just overwrite ONLY if it's NOT a static key.
                 // But wait, the user said "Cybercity should be separated".
                 // So if backend returns 'cybercity', we ignore it.
                 
                 // Simple check: if keys collide, do nothing (keep static).
                 // If keys don't collide, add it.
                 
                 // But what if backend sends numeric IDs (1, 2, 3) and static usages string keys ('cybercity')?
                 // Then they won't collide, and they will coexist. That is perfect.
                 
                 if (!result[item.id]) {
                     result[item.id] = {
                        id: item.id,
                        name: item.name,
                        description: item.description,
                        isDefault: item.isDefault,
                        isOwned: true, // If it comes from 'my items' endpoint, I own it.
                        ...config
                     }
                 }
            });
        }
        return result;
    }

    // 키로 설정 가져오기 헬퍼 (정적 키와 숫자 ID 모두 작동)
    const getItemConfig = (type, key) => {
        // 정의(동적)를 먼저 확인하고, 정적 기본값을 확인
        // 하지만 definition은 ID로 평탄화되어 있음.
        // 정적 기본값은 'cybercity' 같은 키를 사용.
        
        // 키가 정의에 있다면 (숫자 또는 DB ID와 일치하는 문자열)
        if (itemDefinitions.value[key]) return itemDefinitions.value[key];
        
        // 정적 파일로 폴백 (상단에서 가져옴)
        if (type === 'AVATAR') return AVATAR_DECORATIONS[key];
        if (type === 'POPOVER') return POPOVER_DECORATIONS[key];
        if (type === 'THEME') return PROFILE_THEMES[key];
        
        return null;
    }

    return {
        avatarItems,
        popoverItems,
        themeItems,
        fetchUserItems,
        fetchItemDefinitions,
        getItemConfig,
        loading
    }
})



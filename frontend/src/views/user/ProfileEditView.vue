<template>
  <div class="profile-edit-container">
    <div class="container mx-auto px-4 max-w-2xl">
      <Card>
        <template #header>
          <div class="p-4 border-bottom-1 surface-border">
            <h1 class="text-3xl font-bold m-0">프로필 수정</h1>
          </div>
        </template>
        <template #content>
          <div class="flex flex-col gap-6">
            <div class="flex flex-col gap-4">
              <h2 class="text-2xl font-bold">프로필</h2>

              <div class="flex flex-col gap-2 items-center my-8">
                <div class="relative inline-block">

                   <UserAvatar 
                   :user="previewUser"
                   class="font-bold border-4 border-gray-200 surface-200" 
                   style="width: 300px; height: 300px; font-size: 100px"/>

                  <!-- <Avatar
                    :image="previewUrl"
                    :label="!previewUrl && profileForm.nickname ? profileForm.nickname.charAt(0).toUpperCase() : ''"
                    :icon="!previewUrl && !profileForm.nickname ? 'pi pi-user' : ''"
                    class="font-bold border-4 border-gray-200 surface-200"
                    shape="circle"
                  /> -->

                  <div class="absolute bottom-4 right-4">
                    <FileUpload
                      mode="basic"
                      name="file"
                      accept="image/*"
                      :maxFileSize="5000000"
                      @select="onFileSelect"
                      :auto="false"
                      chooseIcon="pi pi-camera"
                      chooseLabel=" "
                      class="custom-upload p-button-rounded p-button-lg shadow-4"
                      style="width: 4rem; height: 4rem"
                    />
                  </div>
                </div>
                <small class="text-gray-500 mt-2">Click camera icon to change image</small>
              </div>

              <!-- 닉네임 변경 부분 수정 -->
              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium">닉네임 변경하기</label>
                <div class="flex gap-2">
                  <InputText v-model="profileForm.nickname" placeholder="닉네임을 써주세요" class="flex-1" :class="{ 'p-invalid': nicknameState.error }" maxlength="10" @input="handleNicknameChange" />
                  <!-- 본인 닉네임일 경우 버튼 비활성화 -->
                  <Button
                    label="중복 확인"
                    icon="pi pi-check-circle"
                    severity="secondary"
                    :loading="nicknameState.isChecking"
                    :disabled="!profileForm.nickname || profileForm.nickname === originalNickname || nicknameState.isChecked"
                    @click="handleCheckNickname"
                  />
                </div>

                <!-- 상태 메시지 표시 -->
                <small v-if="nicknameState.error" class="text-red-500">{{ nicknameState.error }}</small>
                <small v-else-if="profileForm.nickname !== originalNickname && nicknameState.isChecked" class="text-green-500">사용 가능한 닉네임입니다.</small>
                <small v-else class="text-gray-500">닉네임 변경 시 중복 확인이 필요합니다.</small>
              </div>

              <!-- 커스터마이징 섹션 -->
              <Divider />
              <div class="flex flex-col gap-6">
                <h3 class="text-xl font-bold">꾸미기</h3>
                
                <!-- 배경 테마 선택 -->
                <div class="flex flex-col gap-3">
                  <label class="text-sm font-medium">프로필 배경 테마</label>
                  <div class="flex gap-3 overflow-x-auto pb-2">
                    <div 
                      v-for="theme in themes" 
                      :key="theme.key"
                      class="cursor-pointer border-2 rounded-lg p-1 min-w-[80px] h-[60px] overflow-hidden relative"
                      :class="{'border-primary': profileForm.profileTheme === theme.key, 'border-transparent': profileForm.profileTheme !== theme.key}"
                      @click="profileForm.profileTheme = theme.key"
                    >
                      <div v-if="theme.image" class="w-full h-full">
                          <img :src="theme.image" class="w-full h-full object-cover rounded" />
                      </div>
                      <div v-else class="w-full h-full rounded" :class="theme.class" :style="theme.style"></div>
                      
                      <span class="absolute bottom-0 left-0 w-full text-[10px] text-center bg-black/50 text-white truncate px-1">
                          {{ theme.name }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- 아바타 꾸미기 선택 -->
                <div class="flex flex-col gap-3">
                  <label class="text-sm font-medium">아바타 프레임</label>
                  <div class="flex gap-3 overflow-x-auto pb-2">
                    <div 
                      v-for="deco in avatars" 
                      :key="deco.key"
                      class="cursor-pointer border-2 rounded-full p-1 min-w-[70px] h-[70px] relative"
                      :class="{'border-primary': profileForm.avatarDecoration === deco.key, 'border-transparent': profileForm.avatarDecoration !== deco.key}"
                      @click="profileForm.avatarDecoration = deco.key"
                    >
                       <div class="w-full h-full bg-gray-200 rounded-full"></div>
                       <img v-if="deco.image" :src="deco.image" class="absolute inset-0 w-full h-full object-contain" />
                       <span class="absolute -bottom-1 left-0 w-full text-[10px] text-center bg-black/50 text-white rounded-full truncate px-1">
                          {{ deco.name }}
                       </span>
                    </div>
                  </div>
                </div>

                <!-- 팝오버 꾸미기 선택 -->
                <div class="flex flex-col gap-3">
                  <label class="text-sm font-medium">팝오버 테마</label>
                  <div class="flex gap-3 overflow-x-auto pb-2">
                    <div 
                      v-for="pop in popovers" 
                      :key="pop.key"
                      class="cursor-pointer border-2 rounded-lg p-1 min-w-[80px] h-[60px] overflow-hidden relative"
                      :class="{'border-primary': profileForm.popoverDecoration === pop.key, 'border-transparent': profileForm.popoverDecoration !== pop.key}"
                      @click="profileForm.popoverDecoration = pop.key"
                    >
                      <img v-if="pop.image" :src="pop.image" class="w-full h-full object-cover rounded" />
                      <div v-else-if="pop.style" class="w-full h-full rounded flex items-center justify-center" :style="pop.style">
                          <!-- 색상이 있으면 색상 박스로 표시 -->
                      </div>
                      <div v-else class="w-full h-full bg-gray-100 flex items-center justify-center">
                          <span class="text-xs text-gray-500">기본</span>
                      </div>
                      <span class="absolute bottom-0 left-0 w-full text-[10px] text-center bg-black/50 text-white truncate px-1">
                          {{ pop.name }}
                       </span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium">상태 메시지</label>
                <Textarea v-model="profileForm.statusMessage" placeholder="내 상태 기입하기" rows="3" class="w-full" />
              </div>

              <Button label="Save Profile" icon="pi pi-check" :loading="savingProfile" @click="handleSaveProfile" class="mt-2" />
            </div>

            <Divider />
            <div class="flex flex-col gap-4">
              <h2 class="text-2xl font-bold">비밀번호 변경</h2>
              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium">현재 비밀번호</label>
                <Password v-model="passwordForm.currentPassword" toggleMask placeholder="Enter current password" class="w-full" />
              </div>
              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium">새 비밀번호</label>
                <Password v-model="passwordForm.newPassword" toggleMask placeholder="Enter new password" class="w-full" />
              </div>
              <div class="flex flex-col gap-2">
                <label class="text-sm font-medium">비밀번호 다시 입력</label>
                <Password v-model="passwordForm.confirmPassword" toggleMask placeholder="Confirm new password" class="w-full" />
              </div>
              <Button label="Change Password" icon="pi pi-key" :loading="changingPassword" @click="handleChangePassword" class="mt-2" />
            </div>

            <Divider />

            <div class="flex flex-col gap-4 danger-section">
              <h2 class="text-2xl font-bold text-red-500">Danger Zone</h2>
              <div class="danger-box border-round">
                <p class="font-semibold mb-2">Delete Account</p>
                <p class="text-sm text-color-secondary mb-4">Once you delete your account, there is no going back.</p>
                <Button label="Delete Account" icon="pi pi-trash" class="danger-btn" severity="danger" outlined @click="handleDeleteAccount" />
              </div>
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { useConfirm } from "primevue/useconfirm";
import { useAuthStore } from "@/stores/auth";
// checkNickname 추가
// checkNickname 추가
import { updateMyProfile, changePassword, deleteAccount, getMe, checkNickname } from "@/api/user";
import UserAvatar from '@/components/common/UserAvatar.vue' // 유저 아바타 불러오기 
import { resolveImageUrl } from "@/lib/image";
import { PROFILE_THEMES } from "@/constants/themeConfig";
import { AVATAR_DECORATIONS } from "@/constants/avatarConfig";
import { POPOVER_DECORATIONS } from "@/constants/popoverConfig"; 

const router = useRouter();
const toast = useToast();
const confirm = useConfirm();
const authStore = useAuthStore();

const profileForm = ref({
  nickname: "",
  statusMessage: "",
  profileTheme: null,
  avatarDecoration: null,
  popoverDecoration: null,
});

const themes = ref([]);
const avatars = ref([]);
const popovers = ref([]);

//  원래 닉네임 보관용 (변경 여부 판단)
const originalNickname = ref("");

//  닉네임 중복 확인 상태 관리
const nicknameState = ref({
  isChecked: true, // 초기값은 본인 닉네임이므로 true
  isChecking: false,
  error: "",
});

const selectedFile = ref<File | null>(null);
const previewUrl = ref("");

const passwordForm = ref({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const savingProfile = ref(false);
const changingPassword = ref(false);

// 실시간 미리보기를 위한 computed 속성
const previewUser = computed(() => {
    return {
        ...authStore.user,
        ...profileForm.value,
        // 새로 선택한 프로필 이미지가 있다면 (previewUrl이 있다면) 그것을 우선 사용, 아니면 기존 이미지
        profileImageUrl: previewUrl.value || authStore.user?.profileImageUrl
    };
});

const loadProfile = async () => {
  try {
    const user = await getMe();
    profileForm.value.nickname = user.nickname || "";
    profileForm.value.nickname = user.nickname || "";
    profileForm.value.statusMessage = user.statusMessage || "";
    profileForm.value.profileTheme = user.profileTheme || null;
    profileForm.value.avatarDecoration = user.avatarDecoration || null;
    profileForm.value.popoverDecoration = user.popoverDecoration || null;

    //  원래 닉네임 저장 및 상태 초기화
    originalNickname.value = user.nickname || "";
    nicknameState.value.isChecked = true;
    nicknameState.value.error = "";

    // 리소스 로드 (Config 파일 사용)
    themes.value = Object.keys(PROFILE_THEMES).map(key => ({
      key,
      ...PROFILE_THEMES[key]
    }));
    
    avatars.value = Object.keys(AVATAR_DECORATIONS).map(key => ({
      key,
      ...AVATAR_DECORATIONS[key]
    }));

    popovers.value = Object.keys(POPOVER_DECORATIONS).map(key => ({
      key,
      ...POPOVER_DECORATIONS[key]
    }));
    /* 
    // 기존 API 호출 로직 제거
    const [themeList, avatarList, popoverList] = await Promise.all([
      getResources('theme'),
      getResources('avatar'),
      getResources('popover')
    ]); 
    */

    // 서버 이미지 주소 설정
    if (user.profileImageUrl) {
      previewUrl.value = resolveImageUrl(user.profileImageUrl);
    } else {
      previewUrl.value = "";
    }
  } catch (error: any) {
    console.error(error);
  }
};

const onFileSelect = (event: any) => {
  const file = event.files[0];
  if (file) {
    selectedFile.value = file;
    previewUrl.value = URL.createObjectURL(file);
  }
};

//  닉네임 입력 핸들러
const handleNicknameChange = () => {
  nicknameState.value.error = "";

  // 원래 닉네임과 같으면 중복확인 필요 없음
  if (profileForm.value.nickname === originalNickname.value) {
    nicknameState.value.isChecked = true;
  } else {
    // 닉네임이 변경되었으면 중복확인 상태 false
    nicknameState.value.isChecked = false;
  }
};

//  닉네임 중복 확인 핸들러
const handleCheckNickname = async () => {
  const nickname = profileForm.value.nickname;

  if (!nickname || nickname.length < 2) {
    nicknameState.value.error = "닉네임은 2자 이상이어야 합니다.";
    return;
  }

  nicknameState.value.isChecking = true;
  nicknameState.value.error = "";

  try {
    await checkNickname(nickname);
    // 에러가 안 나면 성공
    nicknameState.value.isChecked = true;
    toast.add({ severity: "success", summary: "확인 완료", detail: "사용 가능한 닉네임입니다.", life: 3000 });
  } catch (error: any) {
    nicknameState.value.isChecked = false;
    // 백엔드 에러 메시지 표시
    const msg = error.response?.data?.message || "이미 사용 중인 닉네임입니다.";
    nicknameState.value.error = msg;
    toast.add({ severity: "error", summary: "중복", detail: msg, life: 3000 });
  } finally {
    nicknameState.value.isChecking = false;
  }
};

const handleSaveProfile = async () => {
  // [추가] 저장 전 닉네임 검증 확인
  if (!nicknameState.value.isChecked) {
    toast.add({ severity: "warn", summary: "확인 필요", detail: "닉네임 중복 확인을 해주세요.", life: 3000 });
    return;
  }

  if (nicknameState.value.error) {
    toast.add({ severity: "warn", summary: "확인 필요", detail: "닉네임을 확인해주세요.", life: 3000 });
    return;
  }

  savingProfile.value = true;
  try {
    const formData = new FormData();
    formData.append("data", new Blob([JSON.stringify(profileForm.value)], { type: "application/json" }));

    if (selectedFile.value) {
      formData.append("file", selectedFile.value);
    }

    await updateMyProfile(formData);
    await authStore.refreshUser(); // 변경된 정보(특히 닉네임, 프사) 갱신

    //  저장 성공 후 현재 상태를 '원본'으로 갱신
    originalNickname.value = profileForm.value.nickname;
    nicknameState.value.isChecked = true;

    toast.add({ severity: "success", summary: "성공", detail: "프로필 업데이트 성공", life: 3000 });
    selectedFile.value = null;
  } catch (error: any) {
    toast.add({ severity: "error", summary: "Error", detail: "Failed update", life: 3000 });
  } finally {
    savingProfile.value = false;
  }
};

const handleChangePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.add({
      severity: "warn",
      summary: "Warning",
      detail: "Passwords do not match",
      life: 3000,
    });
    return;
  }

  if (passwordForm.value.newPassword.length < 8) {
    toast.add({
      severity: "warn",
      summary: "Warning",
      detail: "Password must be at least 8 characters",
      life: 3000,
    });
    return;
  }

  changingPassword.value = true;
  try {
    await changePassword(passwordForm.value.currentPassword, passwordForm.value.newPassword);
    passwordForm.value = {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    };
    toast.add({
      severity: "success",
      summary: "Success",
      detail: "Password changed successfully",
      life: 3000,
    });
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: error.response?.data?.message || "Failed to change password",
      life: 3000,
    });
  } finally {
    changingPassword.value = false;
  }
};

const handleDeleteAccount = () => {
  confirm.require({
    message: "Are you absolutely sure? This action cannot be undone.",
    header: "Delete Account",
    icon: "pi pi-exclamation-triangle",
    acceptClass: "p-button-danger",
    accept: async () => {
      const password = prompt("Please enter your password to confirm:");
      if (!password) return;

      try {
        await deleteAccount(password);
        toast.add({
          severity: "success",
          summary: "Success",
          detail: "Account deleted successfully",
          life: 3000,
        });
        authStore.logoutUser();
        router.push("/");
      } catch (error: any) {
        toast.add({
          severity: "error",
          summary: "Error",
          detail: error.response?.data?.message || "Failed to delete account",
          life: 3000,
        });
      }
    },
  });
};

onMounted(() => {
  loadProfile();
});
</script>

<style scoped>
.profile-edit-container {
  min-height: calc(100vh - 200px);
}
.container {
  max-width: 1200px;
  margin: 0 auto;
}

:deep(.custom-upload .p-button-label) {
  display: none;
}

:deep(.custom-upload input[type="file"]) {
  opacity: 0 !important;
  width: 0 !important;
  height: 0 !important;
  position: absolute !important;
  z-index: -1 !important;
  overflow: hidden !important;
}

/* 카메라 버튼 스타일 (둥글게, 중앙 정렬) */
:deep(.p-fileupload-choose) {
  border-radius: 50%;
  padding: 0 !important;
  display: flex !important;
  align-items: center;
  justify-content: center;
}

/* Fallback: hide any plain span or label siblings that display the filename/text
   (covers browser default "No file chosen" rendering and PrimeVue variants) */
:deep(.p-fileupload) span {
  display: none !important;
}

.danger-section {
  gap: 0.75rem;
}

.danger-box {
  background: #fff5f5;
  border: 1px solid #fecdd3;
  padding: 1.25rem;
  transition: transform 0.15s ease, box-shadow 0.18s ease, border-color 0.18s ease, background-color 0.18s ease;
}

.danger-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(255, 107, 107, 0.12);
  border-color: #fda4af;
}

.danger-btn {
  transition: transform 0.12s ease, box-shadow 0.15s ease;
}

.danger-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(255, 107, 107, 0.22);
}

:global([data-theme="dark"] .danger-section h2) {
  color: #fca5a5;
}

:global([data-theme="dark"] .danger-box) {
  background: #1b0f0f;
  border: 1px solid #7f1d1d;
  box-shadow:
    0 20px 44px rgba(0, 0, 0, 0.65),
    inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

:global([data-theme="dark"] .danger-box:hover) {
  border-color: #b91c1c;
  box-shadow:
    0 24px 56px rgba(0, 0, 0, 0.7),
    0 0 0 1px rgba(255, 255, 255, 0.04);
}

:global([data-theme="dark"] .danger-btn) {
  border-color: #f87171 !important;
  color: #fca5a5 !important;
  background: rgba(248, 113, 113, 0.06) !important;
}

:global([data-theme="dark"] .danger-btn:hover) {
  border-color: #ef4444 !important;
  color: #fecaca !important;
  box-shadow: 0 12px 26px rgba(239, 68, 68, 0.3);
  background: rgba(248, 113, 113, 0.12) !important;
}

:global([data-theme="dark"] .danger-btn:active) {
  transform: translateY(0);
}
</style>

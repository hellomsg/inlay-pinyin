#include <jni.h>
#include <rime_api.h>
#include <rime/candidate.h>
#include <rime/composition.h>
#include <rime/context.h>
#include <rime/menu.h>
#include <rime/service.h>

#include <exception>
#include <string>
#include <vector>

namespace {
constexpr char schema[] = "luna_pinyin";
RimeSessionId session = 0;

std::string readString(JNIEnv* env, jstring value) {
    const char* chars = env->GetStringUTFChars(value, nullptr);
    if (!chars) return {};
    std::string result(chars);
    env->ReleaseStringUTFChars(value, chars);
    return result;
}
}

// The Kotlin decoder serializes all calls on its worker thread.
extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_gridime_RimeNative_initialize(JNIEnv* env, jobject, jstring shared, jstring user) {
    if (session) return JNI_TRUE;
    try {
        const auto sharedPath = readString(env, shared);
        const auto userPath = readString(env, user);
        RIME_STRUCT(RimeTraits, traits);
        traits.shared_data_dir = sharedPath.c_str();
        traits.user_data_dir = userPath.c_str();
        traits.app_name = "rime.inlay";
        traits.distribution_name = "Inlay";
        traits.distribution_code_name = "inlay";
        traits.distribution_version = "1";
        traits.min_log_level = 3;
        auto* api = rime_get_api();
        api->setup(&traits);
        api->initialize(&traits);
        if (api->start_maintenance(false)) api->join_maintenance_thread();
        session = api->create_session();
        if (session && api->select_schema(session, schema)) {
            api->set_option(session, "zh_hans", true);
            api->set_option(session, "ascii_mode", false);
            return JNI_TRUE;
        }
        if (session) api->destroy_session(session);
        session = 0;
        api->finalize();
    } catch (const std::exception&) {
        session = 0;
    }
    return JNI_FALSE;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_gridime_RimeNative_candidates(JNIEnv* env, jobject, jstring spelling) {
    const auto raw = readString(env, spelling);
    std::vector<std::string> records;
    try {
        if (session && !raw.empty() && raw.size() <= 64) {
            auto* api = rime_get_api();
            api->clear_composition(session);
            api->set_input(session, raw.c_str());
            auto current = rime::Service::instance().GetSession(session);
            auto* context = current ? current->context() : nullptr;
            if (context && context->input() == raw && !context->composition().empty()) {
                auto menu = context->composition().back().menu;
                if (menu) for (size_t i = 0; i < 48; ++i) {
                    auto candidate = menu->GetCandidateAt(i);
                    if (!candidate) break;
                    if (candidate->start() != 0 || candidate->end() == 0 || candidate->end() > raw.size()) continue;
                    const auto text = candidate->text();
                    if (!text.empty()) records.push_back(std::to_string(candidate->end()) + "\t" + text);
                }
            }
            api->clear_composition(session);
        }
    } catch (const std::exception&) {
        records.clear();
    }
    auto bytes = env->FindClass("[B");
    auto result = env->NewObjectArray(records.size(), bytes, nullptr);
    if (!result) return nullptr;
    for (size_t i = 0; i < records.size(); ++i) {
        const auto& text = records[i];
        auto item = env->NewByteArray(text.size());
        if (!item) return nullptr;
        env->SetByteArrayRegion(item, 0, text.size(), reinterpret_cast<const jbyte*>(text.data()));
        env->SetObjectArrayElement(result, i, item);
        env->DeleteLocalRef(item);
    }
    return result;
}

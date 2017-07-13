/*
 * Copyright 2013-2016 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package configs.testutil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bean484 {

    private static final List<Field> FIELDS =
        Arrays.stream(Bean484.class.getDeclaredFields())
            .filter(f -> f.getName().startsWith("a"))
            .sorted(Comparator.comparing(f -> Integer.parseInt(f.getName().substring(1))))
            .collect(Collectors.toList());

    public static Bean484 fromArray(int[] values) {
        Bean484 b = new Bean484();
        try {
            ListIterator<Field> it = FIELDS.listIterator();
            while (it.hasNext()) {
                int i = it.nextIndex();
                Field f = it.next();
                if (f.getType() == int.class)
                    f.setInt(b, values[i]);
                else
                    f.set(b, values[i]);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return b;
    }

    public List<Integer> values() {
        return Arrays.asList(
            a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20,
            a21, a22, a23, a24, a25, a26, a27, a28, a29, a30, a31, a32, a33, a34, a35, a36, a37, a38, a39, a40,
            a41, a42, a43, a44, a45, a46, a47, a48, a49, a50, a51, a52, a53, a54, a55, a56, a57, a58, a59, a60,
            a61, a62, a63, a64, a65, a66, a67, a68, a69, a70, a71, a72, a73, a74, a75, a76, a77, a78, a79, a80,
            a81, a82, a83, a84, a85, a86, a87, a88, a89, a90, a91, a92, a93, a94, a95, a96, a97, a98, a99, a100,
            a101, a102, a103, a104, a105, a106, a107, a108, a109, a110, a111, a112, a113, a114, a115, a116, a117, a118, a119, a120,
            a121, a122, a123, a124, a125, a126, a127, a128, a129, a130, a131, a132, a133, a134, a135, a136, a137, a138, a139, a140,
            a141, a142, a143, a144, a145, a146, a147, a148, a149, a150, a151, a152, a153, a154, a155, a156, a157, a158, a159, a160,
            a161, a162, a163, a164, a165, a166, a167, a168, a169, a170, a171, a172, a173, a174, a175, a176, a177, a178, a179, a180,
            a181, a182, a183, a184, a185, a186, a187, a188, a189, a190, a191, a192, a193, a194, a195, a196, a197, a198, a199, a200,
            a201, a202, a203, a204, a205, a206, a207, a208, a209, a210, a211, a212, a213, a214, a215, a216, a217, a218, a219, a220,
            a221, a222, a223, a224, a225, a226, a227, a228, a229, a230, a231, a232, a233, a234, a235, a236, a237, a238, a239, a240,
            a241, a242, a243, a244, a245, a246, a247, a248, a249, a250, a251, a252, a253, a254, a255, a256, a257, a258, a259, a260,
            a261, a262, a263, a264, a265, a266, a267, a268, a269, a270, a271, a272, a273, a274, a275, a276, a277, a278, a279, a280,
            a281, a282, a283, a284, a285, a286, a287, a288, a289, a290, a291, a292, a293, a294, a295, a296, a297, a298, a299, a300,
            a301, a302, a303, a304, a305, a306, a307, a308, a309, a310, a311, a312, a313, a314, a315, a316, a317, a318, a319, a320,
            a321, a322, a323, a324, a325, a326, a327, a328, a329, a330, a331, a332, a333, a334, a335, a336, a337, a338, a339, a340,
            a341, a342, a343, a344, a345, a346, a347, a348, a349, a350, a351, a352, a353, a354, a355, a356, a357, a358, a359, a360,
            a361, a362, a363, a364, a365, a366, a367, a368, a369, a370, a371, a372, a373, a374, a375, a376, a377, a378, a379, a380,
            a381, a382, a383, a384, a385, a386, a387, a388, a389, a390, a391, a392, a393, a394, a395, a396, a397, a398, a399, a400,
            a401, a402, a403, a404, a405, a406, a407, a408, a409, a410, a411, a412, a413, a414, a415, a416, a417, a418, a419, a420,
            a421, a422, a423, a424, a425, a426, a427, a428, a429, a430, a431, a432, a433, a434, a435, a436, a437, a438, a439, a440,
            a441, a442, a443, a444, a445, a446, a447, a448, a449, a450, a451, a452, a453, a454, a455, a456, a457, a458, a459, a460,
            a461, a462, a463, a464, a465, a466, a467, a468, a469, a470, a471, a472, a473, a474, a475, a476, a477, a478, a479, a480,
            a481, a482, a483, a484);
    }

    int a1;
    int a2;
    Integer a3;
    int a4;
    int a5;
    Integer a6;
    int a7;
    int a8;
    Integer a9;
    int a10;
    int a11;
    int a12;
    Integer a13;
    int a14;
    int a15;
    Integer a16;
    int a17;
    int a18;
    Integer a19;
    int a20;
    int a21;
    int a22;
    Integer a23;
    int a24;
    int a25;
    Integer a26;
    int a27;
    int a28;
    Integer a29;
    int a30;
    int a31;
    int a32;
    Integer a33;
    int a34;
    int a35;
    Integer a36;
    int a37;
    int a38;
    Integer a39;
    int a40;
    int a41;
    int a42;
    Integer a43;
    int a44;
    int a45;
    Integer a46;
    int a47;
    int a48;
    Integer a49;
    int a50;
    int a51;
    int a52;
    Integer a53;
    int a54;
    int a55;
    Integer a56;
    int a57;
    int a58;
    Integer a59;
    int a60;
    int a61;
    int a62;
    Integer a63;
    int a64;
    int a65;
    Integer a66;
    int a67;
    int a68;
    Integer a69;
    int a70;
    int a71;
    int a72;
    Integer a73;
    int a74;
    int a75;
    Integer a76;
    int a77;
    int a78;
    Integer a79;
    int a80;
    int a81;
    int a82;
    Integer a83;
    int a84;
    int a85;
    Integer a86;
    int a87;
    int a88;
    Integer a89;
    int a90;
    int a91;
    int a92;
    Integer a93;
    int a94;
    int a95;
    Integer a96;
    int a97;
    int a98;
    Integer a99;
    int a100;
    int a101;
    int a102;
    Integer a103;
    int a104;
    int a105;
    Integer a106;
    int a107;
    int a108;
    Integer a109;
    int a110;
    int a111;
    int a112;
    Integer a113;
    int a114;
    int a115;
    Integer a116;
    int a117;
    int a118;
    Integer a119;
    int a120;
    int a121;
    int a122;
    Integer a123;
    int a124;
    int a125;
    Integer a126;
    int a127;
    int a128;
    Integer a129;
    int a130;
    int a131;
    int a132;
    Integer a133;
    int a134;
    int a135;
    Integer a136;
    int a137;
    int a138;
    Integer a139;
    int a140;
    int a141;
    int a142;
    Integer a143;
    int a144;
    int a145;
    Integer a146;
    int a147;
    int a148;
    Integer a149;
    int a150;
    int a151;
    int a152;
    Integer a153;
    int a154;
    int a155;
    Integer a156;
    int a157;
    int a158;
    Integer a159;
    int a160;
    int a161;
    int a162;
    Integer a163;
    int a164;
    int a165;
    Integer a166;
    int a167;
    int a168;
    Integer a169;
    int a170;
    int a171;
    int a172;
    Integer a173;
    int a174;
    int a175;
    Integer a176;
    int a177;
    int a178;
    Integer a179;
    int a180;
    int a181;
    int a182;
    Integer a183;
    int a184;
    int a185;
    Integer a186;
    int a187;
    int a188;
    Integer a189;
    int a190;
    int a191;
    int a192;
    Integer a193;
    int a194;
    int a195;
    Integer a196;
    int a197;
    int a198;
    Integer a199;
    int a200;
    int a201;
    int a202;
    Integer a203;
    int a204;
    int a205;
    Integer a206;
    int a207;
    int a208;
    Integer a209;
    int a210;
    int a211;
    int a212;
    Integer a213;
    int a214;
    int a215;
    Integer a216;
    int a217;
    int a218;
    Integer a219;
    int a220;
    int a221;
    int a222;
    Integer a223;
    int a224;
    int a225;
    Integer a226;
    int a227;
    int a228;
    Integer a229;
    int a230;
    int a231;
    int a232;
    Integer a233;
    int a234;
    int a235;
    Integer a236;
    int a237;
    int a238;
    Integer a239;
    int a240;
    int a241;
    int a242;
    Integer a243;
    int a244;
    int a245;
    Integer a246;
    int a247;
    int a248;
    Integer a249;
    int a250;
    int a251;
    int a252;
    Integer a253;
    int a254;
    int a255;
    Integer a256;
    int a257;
    int a258;
    Integer a259;
    int a260;
    int a261;
    int a262;
    Integer a263;
    int a264;
    int a265;
    Integer a266;
    int a267;
    int a268;
    Integer a269;
    int a270;
    int a271;
    int a272;
    Integer a273;
    int a274;
    int a275;
    Integer a276;
    int a277;
    int a278;
    Integer a279;
    int a280;
    int a281;
    int a282;
    Integer a283;
    int a284;
    int a285;
    Integer a286;
    int a287;
    int a288;
    Integer a289;
    int a290;
    int a291;
    int a292;
    Integer a293;
    int a294;
    int a295;
    Integer a296;
    int a297;
    int a298;
    Integer a299;
    int a300;
    int a301;
    int a302;
    Integer a303;
    int a304;
    int a305;
    Integer a306;
    int a307;
    int a308;
    Integer a309;
    int a310;
    int a311;
    int a312;
    Integer a313;
    int a314;
    int a315;
    Integer a316;
    int a317;
    int a318;
    Integer a319;
    int a320;
    int a321;
    int a322;
    Integer a323;
    int a324;
    int a325;
    Integer a326;
    int a327;
    int a328;
    Integer a329;
    int a330;
    int a331;
    int a332;
    Integer a333;
    int a334;
    int a335;
    Integer a336;
    int a337;
    int a338;
    Integer a339;
    int a340;
    int a341;
    int a342;
    Integer a343;
    int a344;
    int a345;
    Integer a346;
    int a347;
    int a348;
    Integer a349;
    int a350;
    int a351;
    int a352;
    Integer a353;
    int a354;
    int a355;
    Integer a356;
    int a357;
    int a358;
    Integer a359;
    int a360;
    int a361;
    int a362;
    Integer a363;
    int a364;
    int a365;
    Integer a366;
    int a367;
    int a368;
    Integer a369;
    int a370;
    int a371;
    int a372;
    Integer a373;
    int a374;
    int a375;
    Integer a376;
    int a377;
    int a378;
    Integer a379;
    int a380;
    int a381;
    int a382;
    Integer a383;
    int a384;
    int a385;
    Integer a386;
    int a387;
    int a388;
    Integer a389;
    int a390;
    int a391;
    int a392;
    Integer a393;
    int a394;
    int a395;
    Integer a396;
    int a397;
    int a398;
    Integer a399;
    int a400;
    int a401;
    int a402;
    Integer a403;
    int a404;
    int a405;
    Integer a406;
    int a407;
    int a408;
    Integer a409;
    int a410;
    int a411;
    int a412;
    Integer a413;
    int a414;
    int a415;
    Integer a416;
    int a417;
    int a418;
    Integer a419;
    int a420;
    int a421;
    int a422;
    Integer a423;
    int a424;
    int a425;
    Integer a426;
    int a427;
    int a428;
    Integer a429;
    int a430;
    int a431;
    int a432;
    Integer a433;
    int a434;
    int a435;
    Integer a436;
    int a437;
    int a438;
    Integer a439;
    int a440;
    int a441;
    int a442;
    Integer a443;
    int a444;
    int a445;
    Integer a446;
    int a447;
    int a448;
    Integer a449;
    int a450;
    int a451;
    int a452;
    Integer a453;
    int a454;
    int a455;
    Integer a456;
    int a457;
    int a458;
    Integer a459;
    int a460;
    int a461;
    int a462;
    Integer a463;
    int a464;
    int a465;
    Integer a466;
    int a467;
    int a468;
    Integer a469;
    int a470;
    int a471;
    int a472;
    Integer a473;
    int a474;
    int a475;
    Integer a476;
    int a477;
    int a478;
    Integer a479;
    int a480;
    int a481;
    int a482;
    Integer a483;
    int a484;
}

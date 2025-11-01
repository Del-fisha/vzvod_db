package com.company.vzvod.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum MetroStation implements EnumClass<Integer> {

    // Линия 1: Кировско-Выборгская (красная)
    DEVYATKINO(101),
    GRAZHDANSKY_PROSPEKT(102),
    AKADEMICHESKAYA(103),
    POLITEHNICHESKAYA(104),
    PLOSHCHAD_MUZHESTVA(105),
    LESNAYA(106),
    VYBORGSKAYA(107),
    PLOSHCHAD_LENINA(108),
    CHERNYSHEVSKAYA(109),
    PLOSHCHAD_VOSSTANIYA(110),
    VLADIMIRSKAYA(111),
    PUSHKINSKAYA(112),
    TEHNOLOGICHESKY_INSTITUT_1(113),
    BALTIYSKAYA(114),
    NARVSKAYA(115),
    KIROVSKY_ZAVOD(116),
    AVTOVO(117),
    LENINSKY_PROSPEKT(118),
    PROSPEKT_VETERANOV(119),

    // Линия 2: Московско-Петроградская (синяя)
    PARNAS(201),
    PROSPEKT_PROSVESHCHENIYA(202),
    OZERKI(203),
    UDELNAYA(204),
    PIONERSKAYA(205),
    CHORNAYA_RECHKA(206),
    PETROGRADSKAYA(207),
    GORKOVSKAYA(208),
    NEVSKY_PROSPEKT(209),
    SENNAYA_PLOSHCHAD(210),
    TEHNOLOGICHESKY_INSTITUT_2(211),
    FRUNZENSKAYA(212),
    MOSKOVSKIE_VOROTA(213),
    ELEKTROSILA(214),
    PARK_POBEDY(215),
    MOSKOVSKAYA(216),
    ZVYOZDNAYA(217),
    KUPCHINO(218),

    // Линия 3: Невско-Василеостровская (зеленая)
    BEGOVAYA(301),
    ZENIT(302),
    PRIMORSKAYA(303),
    VASILEOSTROVSKAYA(304),
    GOSTINY_DVOR(305),
    MAYAKOVSKAYA(306),
    PLOSHCHAD_ALEKSANDRA_NEVSKOGO_1(307),
    ELIZAROVSKAYA(308),
    LOMONOSOVSKAYA(309),
    PROLETARSKAYA(310),
    OBUKHOVO(311),
    RYBATSKOYE(312),

    // Линия 4: Лахтинско-Правобережная (оранжевая)
    GORNY_INSTITUT(401),
    SPASSKAYA(402),
    DOSTOEVSKAYA(403),
    LIGOVSKY_PROSPEKT(404),
    PLOSHCHAD_ALEKSANDRA_NEVSKOGO_2(405),
    NOVOCHERKASSKAYA(406),
    LADOZHSKAYA(407),
    PROSPEKT_BOLSHEVIKOV(408),
    ULITSA_DYBENKO(409),

    // Линия 5: Фрунзенско-Приморская (фиолетовая)
    KOMENDANTSKY_PROSPEKT(501),
    STARAYA_DEREVNYA(502),
    KRESTOVSKY_OSTROV(503),
    CHKALOVSKAYA(504),
    SPORTIVNAYA(505),
    ADMIRALTEYSKAYA(506),
    SADOVAYA(507),
    ZVENIGORODSKAYA(508),
    OBVODNY_KANAL(509),
    VOLKOVSKAYA(510),
    BUKHARESTSKAYA(511),
    MEZHDUNARODNAYA(512),
    PROSPEKT_SLAVY(513),
    DUNAYSKAYA(514),
    SHUSHARY(515);

    private final Integer id;

    MetroStation(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static MetroStation fromId(Integer id) {
        for (MetroStation at : MetroStation.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}

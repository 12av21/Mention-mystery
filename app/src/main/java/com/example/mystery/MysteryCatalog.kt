package com.example.mystery

import com.example.R
import com.example.model.ClueCategory
import com.example.model.ClueDefinition
import com.example.model.ClueId
import com.example.model.EvidenceLinkDefinition
import com.example.model.PuzzleType
import com.example.model.RoomId
import com.example.model.RoomPuzzleDefinition

object MysteryCatalog {

    val clues: List<ClueDefinition> = listOf(
        ClueDefinition(
            id = ClueId.CLUE_CIPHER_FOLIO,
            title = "Torn Cipher Folio",
            roomId = RoomId.LIBRARY,
            category = ClueCategory.DOCUMENT,
            shortSummary = "Bookplate cipher naming a midnight rendezvous.",
            fullDescription = "Tucked inside a hollowed volume of Astral Mechanics. Margins bear Lord Ravenshollow's crimson seal: 'Meet beneath the painted gaze at 11:45—bring the Star-Compass.'",
            boardSnippet = "Rendezvous set for 11:45 PM beneath the Portrait Gallery.",
            imageRes = R.drawable.img_detective_portrait,
        ),
        ClueDefinition(
            id = ClueId.CLUE_FORGED_LEDGER,
            title = "Forged Estate Ledger",
            roomId = RoomId.STUDY,
            category = ClueCategory.DOCUMENT,
            shortSummary = "Altered accounts proving embezzlement by Dr. Ashford.",
            fullDescription = "Double-entry ledger from the Study desk. Ink analysis reveals Dr. Julian Ashford doctored three shipping manifests at 11:30 PM to conceal missing vault gold.",
            boardSnippet = "Dr. Ashford forged the shipping ledger at 11:30 PM.",
            imageRes = R.drawable.img_lord_portrait,
        ),
        ClueDefinition(
            id = ClueId.CLUE_MOONBLOOM_VIAL,
            title = "Moonbloom Vial",
            roomId = RoomId.CONSERVATORY,
            category = ClueCategory.FORENSIC,
            shortSummary = "Rare nocturnal sedative harvested from lunar orchids.",
            fullDescription = "A crystal dropper stained with glowing indigo sap from the Conservatory's rare Nightshade-Orchid hybrid—known to induce deep sleep without trace in warm tea.",
            boardSnippet = "Night-blooming sedative extracted in the Conservatory.",
            imageRes = R.drawable.img_detective_portrait,
        ),
        ClueDefinition(
            id = ClueId.CLUE_BITTER_TEACUP,
            title = "Bitter Almond Teacup",
            roomId = RoomId.KITCHEN,
            category = ClueCategory.FORENSIC,
            shortSummary = "Porcelain cup laced with honey and Moonbloom extract.",
            fullDescription = "Left unwashed beside the Kitchen hearth. Residue matches the Moonbloom sedative masked with wildflower honey—prepared specifically for Lord Ravenshollow's nightcap.",
            boardSnippet = "Sedative was disguised in Lord Ravenshollow's midnight tea.",
            imageRes = R.drawable.img_royal_heirloom,
        ),
        ClueDefinition(
            id = ClueId.CLUE_CREST_MEDALLION,
            title = "Ravenshollow Crest Key",
            roomId = RoomId.GALLERY,
            category = ClueCategory.RELIC,
            shortSummary = "Secret vault medallion hidden behind the Founder's oil painting.",
            fullDescription = "A heavy brass medallion bearing the Star-Compass crest. Scratches along its teeth show it was used just before midnight to seal the Grand Secret Room.",
            boardSnippet = "Used to lock the Star-Compass inside the Grand Secret Room.",
            imageRes = R.drawable.img_lord_portrait,
        ),
    )

    val evidenceLinks: List<EvidenceLinkDefinition> = listOf(
        EvidenceLinkDefinition(
            id = "link_sedative_tea",
            first = ClueId.CLUE_MOONBLOOM_VIAL,
            second = ClueId.CLUE_BITTER_TEACUP,
            insightTitle = "The Drugged Nightcap",
            insightBody = "Moonbloom sap from the Conservatory was steeped with honey in the Kitchen to incapacitate Lord Ravenshollow before midnight.",
        ),
        EvidenceLinkDefinition(
            id = "link_motive_time",
            first = ClueId.CLUE_FORGED_LEDGER,
            second = ClueId.CLUE_CIPHER_FOLIO,
            insightTitle = "Motive & Timeline",
            insightBody = "Dr. Ashford forged the ledger at 11:30 PM, then intercepted Lord Ravenshollow's 11:45 PM rendezvous note.",
        ),
        EvidenceLinkDefinition(
            id = "link_vault_seal",
            first = ClueId.CLUE_CIPHER_FOLIO,
            second = ClueId.CLUE_CREST_MEDALLION,
            insightTitle = "The Hidden Sanctuary",
            insightBody = "Following the cipher note to the Portrait Gallery led straight to the Crest Key that seals the Grand Secret Room.",
        ),
    )

    val puzzles: List<RoomPuzzleDefinition> = listOf(
        RoomPuzzleDefinition(
            id = "puzzle_library",
            roomId = RoomId.LIBRARY,
            title = "The Spine Cipher",
            subtitle = "Library Lock Mechanism",
            type = PuzzleType.CIPHER_BOOKS,
            prompt = "Four gilded folio spines guard the Library latch. Read the engraved verse and tap the four volumes in order to spell the midnight password:",
            hints = listOf(
                "Look at the capitalized initials in the plaque verse: Night, Onyx, Crown, Torch.",
                "Spell the Latin root for 'Night': N - O - C - T.",
            ),
            rewardClueId = ClueId.CLUE_CIPHER_FOLIO,
            expectedSequence = listOf("N - Nightfall", "O - Onyx", "C - Crown", "T - Torch"),
            options = listOf("C - Crown", "T - Torch", "N - Nightfall", "S - Scepter", "O - Onyx", "V - Velvet"),
            extraNotes = listOf(
                "Plaque Verse: 'First falls the Night, then shines the Onyx stone; a fallen Crown awaits the Torch alone.'",
            ),
        ),
        RoomPuzzleDefinition(
            id = "puzzle_study",
            roomId = RoomId.STUDY,
            title = "The Alibi Contradiction",
            subtitle = "Study Brass Dial",
            type = PuzzleType.LOGIC_DEDUCTION,
            prompt = "Three guests claimed innocence on the night of the theft. Cross-check their statements against the desk notes and select BOTH the true Forger and the exact Time of entry:",
            hints = listOf(
                "Lady Clara was seen in the Conservatory until 11:40 PM, so she could not be in the Study at 11:30 PM.",
                "Only Dr. Ashford's ink blot was still wet at 11:30 PM.",
            ),
            rewardClueId = ClueId.CLUE_FORGED_LEDGER,
            expectedSequence = listOf("Dr. Julian Ashford", "11:30 PM"),
            options = listOf(
                "Dr. Julian Ashford",
                "Lady Clara Vance",
                "Butler Hawthorne",
                "10:45 PM",
                "11:30 PM",
                "12:15 AM",
            ),
            extraNotes = listOf(
                "Note 1: 'Whoever entered the Study spilled crimson ink 15 minutes before the 11:45 PM rendezvous.'",
                "Note 2: 'Hawthorne never leaves the Kitchen hearth before midnight; Clara remained among the orchids.'",
            ),
        ),
        RoomPuzzleDefinition(
            id = "puzzle_conservatory",
            roomId = RoomId.CONSERVATORY,
            title = "Lunar Bloom Matrix",
            subtitle = "Conservatory Latch",
            type = PuzzleType.BOTANICAL_PATTERN,
            prompt = "The glasshouse lock responds to moonlit symmetry. Select the 3 botanical tiles that mirror the eastern petals across the center vine:",
            hints = listOf(
                "Count the petals on the left column and mirror them horizontally onto the empty right-side beds.",
                "Activate tiles: Top-Right (R1C3), Middle-Center (R2C2), and Bottom-Right (R3C3).",
            ),
            rewardClueId = ClueId.CLUE_MOONBLOOM_VIAL,
            expectedSequence = listOf("R1C3: Moon Lily", "R2C2: Night Orchid", "R3C3: Silver Fern"),
            options = listOf(
                "R1C1: Thorn Rose",
                "R1C2: Moss",
                "R1C3: Moon Lily",
                "R2C1: Ivy",
                "R2C2: Night Orchid",
                "R2C3: Belladonna",
                "R3C1: Clover",
                "R3C2: Briar",
                "R3C3: Silver Fern",
            ),
            extraNotes = listOf(
                "Botanist's Sketch: 'Left petals bloom at Dawn, but the Secret Sap wakes only along the East-Center Mirror: Lily high, Orchid heart, Fern low.'",
            ),
        ),
        RoomPuzzleDefinition(
            id = "puzzle_kitchen",
            roomId = RoomId.KITCHEN,
            title = "The Midnight Nightcap",
            subtitle = "Kitchen Pantry Lock",
            type = PuzzleType.RECIPE_SEQUENCE,
            prompt = "Reconstruct the exact 4-step preparation sequence found scribbled beside the pantry lock to release the brass bolt:",
            hints = listOf(
                "Start by heating water in the copper kettle, then steep the nocturnal herb before masking its bitterness.",
                "Correct order: 1. Boil Kettle -> 2. Steep Moonbloom -> 3. Stir Wild Honey -> 4. Wipe Rim Clean.",
            ),
            rewardClueId = ClueId.CLUE_BITTER_TEACUP,
            expectedSequence = listOf(
                "Boil Copper Kettle",
                "Steep Moonbloom Drops",
                "Stir Wildflower Honey",
                "Wipe Porcelain Rim",
            ),
            options = listOf(
                "Stir Wildflower Honey",
                "Wipe Porcelain Rim",
                "Boil Copper Kettle",
                "Steep Moonbloom Drops",
            ),
            extraNotes = listOf(
                "Chef's Note: 'Heat precedes infusion; sweetness masks poison; silence erases traces.'",
            ),
        ),
        RoomPuzzleDefinition(
            id = "puzzle_gallery",
            roomId = RoomId.GALLERY,
            title = "Ancestral Crest Seal",
            subtitle = "Gallery Tumbler",
            type = PuzzleType.GALLERY_SYMBOLS,
            prompt = "Three oil portraits line the Gallery wall. Pair each ancestor's title with their true heraldic symbol in order (Founder, Scholar, Astronomer):",
            hints = listOf(
                "The Founder wears a Crown, the Scholar holds a Serpent staff, and the Astronomer gazes at a Star-Compass.",
                "Select in order: Crown, Serpent, Star-Compass.",
            ),
            rewardClueId = ClueId.CLUE_CREST_MEDALLION,
            expectedSequence = listOf("Crown (Founder)", "Serpent (Scholar)", "Star-Compass (Astronomer)"),
            options = listOf(
                "Hourglass (Keeper)",
                "Serpent (Scholar)",
                "Raven (Envoy)",
                "Crown (Founder)",
                "Star-Compass (Astronomer)",
                "Dagger (Sentinel)",
            ),
            extraNotes = listOf(
                "Gallery Plaque: 'First came the Crown that built these walls, next the Serpent of hidden lore, last the Star-Compass that guards our final secret.'",
            ),
        ),
    )

    val finalDeductionPrompt =
        "Using your collected evidence, complete the final deduction to unseal the Ravenshollow Star-Compass vault:"

    val finalSuspectOptions = listOf(
        "Dr. Julian Ashford",
        "Lady Clara Vance",
        "Butler Hawthorne",
    )

    val finalMethodOptions = listOf(
        "Moonbloom-laced honey tea",
        "Secret passage ambush",
        "Poisoned fountain pen",
    )

    val finalMotiveOptions = listOf(
        "Hide ledger embezzlement & steal the Star-Compass",
        "Revenge for an unpaid gardener's wage",
        "Win a wager with the local constabulary",
    )

    fun clueById(id: ClueId): ClueDefinition = clues.first { it.id == id }
    fun clueByStringId(id: String): ClueDefinition? = runCatching { clueById(ClueId.valueOf(id)) }.getOrNull()
    fun puzzleById(id: String): RoomPuzzleDefinition? = puzzles.firstOrNull { it.id == id }
    fun puzzleForRoom(roomId: RoomId): RoomPuzzleDefinition? = puzzles.firstOrNull { it.roomId == roomId }
}

object PuzzleValidator {
    fun validate(puzzle: RoomPuzzleDefinition, selectedSequence: List<String>): Boolean {
        return when (puzzle.type) {
            PuzzleType.CIPHER_BOOKS,
            PuzzleType.RECIPE_SEQUENCE,
            PuzzleType.GALLERY_SYMBOLS -> selectedSequence == puzzle.expectedSequence

            PuzzleType.LOGIC_DEDUCTION,
            PuzzleType.BOTANICAL_PATTERN ->
                selectedSequence.size == puzzle.expectedSequence.size &&
                    selectedSequence.toSet() == puzzle.expectedSequence.toSet()
        }
    }

    fun validateSecretFinale(
        suspect: String?,
        method: String?,
        motive: String?,
        collectedCluesCount: Int,
    ): Boolean {
        if (collectedCluesCount < ClueId.entries.size) return false
        return suspect == "Dr. Julian Ashford" &&
            method == "Moonbloom-laced honey tea" &&
            motive == "Hide ledger embezzlement & steal the Star-Compass"
    }
}

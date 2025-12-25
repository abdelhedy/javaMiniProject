// Skills Functions
async function loadSkills() {
    try {
        allSkills = await SkillsAPI.getAll();
    } catch (error) {
        console.error('Error loading skills:', error);
    }
}
